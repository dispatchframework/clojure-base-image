(ns func-server.main
  (:require [clojure.repl :as repl]
            [func-server.routes :refer [app]]
            [org.httpkit.server :as hk])
  (:gen-class))


(defn run-server [f]
  (do
    (println "Runtime API server for" (repl/demunge (str f)))
    (hk/run-server (app f) {:port 8080})))


(defn -dummy [context payload] payload)


(defn -main []
  (run-server -dummy))
