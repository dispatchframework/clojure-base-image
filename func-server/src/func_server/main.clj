(ns func-server.main
  (:require [clojure.edn :as edn]
            [clojure.repl :as repl]
            [environ.core :refer [env]]
            [func-server.routes :refer [app]]
            [org.httpkit.server :as hk])
  (:gen-class))


(defn run-server [f]
  (do
    (println "Runtime API server for" (repl/demunge (str f)))
    (hk/run-server (app f) {:port (-> env :port edn/read-string (or 8080))})))


(defn -dummy [context payload] payload)


(defn fn-by-fqn [s]
  (let [s (symbol s)]
    (require (-> s namespace symbol))
    @(resolve s)))


(defn- -main [handler]
  (run-server (fn-by-fqn handler)))
