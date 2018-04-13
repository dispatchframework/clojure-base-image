(ns func-server.routes
  (:require [compojure.route :as route]
            [func-server.core :as core])
  (:use [compojure.core]
        [ring.middleware.json]))


(defn handle-invocation [f]
  (fn [req]
    {:body (f (:body req))}))


(defn handle [f]
  (routes
    (POST "/*" [] (handle-invocation f))

    (GET "/healthz" [] {:body {}})

    (route/not-found {:body {:message "404 Not Found"}})))


(defn app [f]
  (-> (core/wrap-func f)
      handle
      (wrap-json-body {:keywords?    true
                       :bigdecimals? true})
      wrap-json-response))
