(ns func-server.main-test
  (:require [clojure.test :refer :all]
            [func-server.main :refer :all]))


(defn test-fn [])


(deftest get-fn-by-fqn
  (is (identical? test-fn
                  (fn-by-fqn (str 'func-server.main-test/test-fn)))))


(run-tests)
