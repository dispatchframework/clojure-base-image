(ns func-server.read-ns
  (:require [clojure.edn :as edn]))

(defn get-ns[path]
  (some-> path
          slurp
          edn/read-string
          seq
          (#(when (= 'ns (first %)) %))
          second
          (#(when (symbol? %) %))))

(defn- -main [path]
  (if-let [n (get-ns path)]
    (print n)
    (System/exit 1)))
