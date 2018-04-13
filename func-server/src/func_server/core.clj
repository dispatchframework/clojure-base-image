(ns func-server.core
  (:require [clojure.tools.logging :as log]
            [clojure.tools.logging.impl :as li]
            [clojure.string :as string]
            [clojure.spec.alpha :as s]))


(defn- logs? [logs]
  (and (instance? clojure.lang.Atom logs)
       (vector? @logs)))

(defn- ns? [n]
  (instance? clojure.lang.Namespace n))


(defn write-log! [logs message]
  (swap! logs conj message))

(s/fdef write-log!
  :args (s/cat :logs logs?
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
        ::stderr (write-log! logs message)
        ::stdout (write-log! logs message)
        (write-log! logs (format-msg ns level message throwable))))))

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


(defn capture-logs [f]
  (let [function-ns (create-ns 'function)]
    (fn [message]
      (let [logs (atom [])]
        (binding [log/*logger-factory* (logger-factory logs)]
          (log/with-logs [function-ns ::stdout ::stderr]
            (try
              (log/log-capture! function-ns ::stdout ::stderr)
              (update-in (f message) [:context :logs] (constantly @logs))
              (finally
                (log/log-uncapture!)))))))))


(defn wrap-func [f]
  (-> f
      capture-error
      capture-logs))
