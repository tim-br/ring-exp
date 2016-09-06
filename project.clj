(defproject clj-tpl-chess "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [metosin/ring-http-response "0.8.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [cheshire "5.6.3"]
                 [org.apache.commons/commons-lang3 "3.4"]
                 [postgresql "9.3-1102.jdbc41"]
                 [org.clojure/java.jdbc "0.6.2-alpha2"]
                 [clojurewerkz/scrypt "1.2.0"]
                 [com.cognitect/transit-clj "0.8.288"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler clj-tpl-chess.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
