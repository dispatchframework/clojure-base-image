(defproject func-server "0.1.0-SNAPSHOT"
  :description "Dispatch Function Runtime API Server for Clojure"
  :url "http://example.com/FIXME"
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [environ "1.1.0"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-json "0.5.0-beta1"]
                 [cheshire "5.8.0"]]
  :aot :all
  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]
  :main func-server.main)
