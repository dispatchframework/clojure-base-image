(ns func-server.core
  (:require [clojure.tools.logging :as log]
            [clojure.tools.logging.impl :as li]
            [clojure.string :as string]
            [clojure.spec.alpha :as s])
  (:import (clojure.lang Atom
                         Namespace)))


(defn- logs? [logs]
  (and (instance? Atom logs)
       (map? @logs)
       (vector? (:stdout @logs))
       (vector? (:stderr @logs))))


(defn- ns? [n]
  (instance? Namespace n))

(defn- stream-kw? [k]
  (or (= :stdout k)
      (= :stderr k)))


(defn write-log! [logs stream-kw message]
  (swap! logs #(update % stream-kw conj message)))


(s/fdef write-log!
  :args (s/cat :logs logs?
               :stream-kw stream-kw?
               :message string?))


(defn level-name [level]
  (-> level
      name
      string/upper-case))

(s/fdef level-name
  :args (s/cat :level keyword?))


(defn format-msg [ns level message throwable]
  (str (level-name level) ": " ns ": " message (if throwable " - " throwable)))


(defn- new-logger [logs ns]
  (reify li/Logger
    (enabled? [_ _] true)
    (write! [_ level throwable message]
      (case level
        ::stderr (write-log! logs :stderr message)
        ::stdout (write-log! logs :stdout message)
        (write-log! logs :stderr (format-msg ns level message throwable))))))

(def logger (memoize new-logger))

(s/fdef logger
  :args (s/cat :logs logs?
               :ns ns?))


(defn logger-factory [logs]
  (reify li/LoggerFactory
    (name [_] "function")
    (get-logger [_ ns]
      (logger logs ns))))

(s/fdef logger-factory
  :args (s/cat :logs logs?))


(defn capture-error [f]
  (fn [{:keys [context payload]}]
    (try
      {:context {:error nil}
       :payload (f context payload)}
      (catch Exception e
        {:context {:error {:message    (-> e .getMessage)
                           :stackTrace (->> e .getStackTrace vec (map str))}}
         :payload nil}))))


(def ^:private function-ns (create-ns 'function))

(defn capture-logs [f]
  (fn [x]
    (let [logs (atom {:stdout []
                      :stderr []})]
      (binding [log/*logger-factory* (logger-factory logs)]
        (log/with-logs [function-ns ::stdout ::stderr]
          (try
            (log/log-capture! function-ns ::stdout ::stderr)
            (-> (f x)
                (update-in [:context :logs] (constantly @logs)))
            (finally
              (log/log-uncapture!))))))))


(defn wrap-func [f]
  (-> f
      capture-error
      capture-logs))
