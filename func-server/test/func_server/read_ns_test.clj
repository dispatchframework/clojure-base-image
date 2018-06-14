(ns func-server.read-ns-test
  (:require [clojure.test :refer :all]
            [func-server.read-ns :refer :all]))


(deftest get-ns-test
  (is (= 'func-server.core
         (get-ns "src/func_server/core.clj")))
  (is (= nil
         (get-ns "project.clj"))))


(run-tests)
