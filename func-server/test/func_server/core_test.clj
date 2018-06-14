(ns func-server.core-test
  (:require [clojure.test :refer :all]
            [func-server.core :refer :all]
            [clojure.spec.test.alpha :as stest]))


(defn- throw-ex1 [_ _]
  (throw (Exception. "my exception message")))


(deftest throwing-exceptions
  (let [params     {:context nil
                    :payload nil}
        wrapped-fn (capture-error throw-ex1)
        result     (wrapped-fn params)]
    (is (= "my exception message")
        (-> result
            :context
            :error
            :message))
    (is (= "func_server.core_test$throw_ex1.invokeStatic(core_test.clj:7)"
           (-> result
               :context
               :error
               :stackTrace
               (nth 0))))))


(defn- logging-wrapped-fn [{:keys [context payload]}]
  (.println System/err "Some")
  (.println System/err "logs")
  {:context context
   :payload payload})

(deftest capture-logs-test
  (is (= ["Some" "logs"]
         (-> ((capture-logs logging-wrapped-fn) {:payload "Hey!"})
             :context
             :logs
             :stderr))))


(defn- logging-*-fn [context payload]
  (println "Arg:" payload)
  (let [r (* payload (-> context
                         :secrets
                         :factor))]
    (println "Result:" r)
    (flush)
    r))

(deftest wrap-func-test
  (let [wf  (wrap-func logging-*-fn)
        msg (wf {:context {:secrets {:factor 7}}
                 :payload 2})]
    (is (= 14
           (:payload msg)))
    (is (= ["Arg: 2" "Result: 14"]
           (-> msg :context :logs :stdout)))))


(stest/instrument)
(run-tests)
