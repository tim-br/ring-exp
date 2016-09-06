(ns clj-tpl-chess.handler
  (:require [cheshire.core :refer :all]
            [clojure.java.jdbc :as j]
            [clj-tpl-chess.htmp-tmpl :as page]
            [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojurewerkz.scrypt.core :as sc]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :as def]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.anti-forgery :refer :all]
            [ring.middleware.params :as params]
            [ring.middleware.session.cookie :refer (cookie-store)]
            [ring.util.response :as ra]
            [ring.util.http-response :refer [ok]]
            [clj-tpl-chess.token :refer [gen-token]]
            [cognitect.transit :as transit])
  (:import (java.io ByteArrayInputStream)))

(def pg-conn
  {:classname   "org.postgresql.Driver"                     ; must be in classpath
   :subprotocol "postgresql"
   :subname     "//127.0.0.1:5432/timothy"
   :user        "timothy"})

(defn exec-db-command [db command]
  (j/with-db-connection [conn db]
                        (with-open [s (.createStatement (:connection conn))]
                          (.executeUpdate s command))))

(defn create-chess-db
  []
  (exec-db-command pg-conn "CREATE DATABASE chess_site"))

(defn create-videos-table
  []
  (exec-db-command pg-conn "CREATE TABLE videos"))

(def pg-conn-chess
  {:classname   "org.postgresql.Driver"                     ; must be in classpath
   :subprotocol "postgresql"
   :subname     "//127.0.0.1:5432/chess_site"
   :user        "timothy"})

(defn create-tokens-table
  []
  (exec-db-command pg-conn-chess (str "CREATE TABLE tokens(
    id SERIAL PRIMARY KEY,
    description text,  enc_token text NOT NULL,  created_at timestamp,
      updated_at timestamp, user_id integer)")))

(defn create-users-table
  []
  (exec-db-command pg-conn-chess (str "CREATE TABLE users(
    user_id SERIAL PRIMARY KEY,
      user_name text NOT NULL, enc_password text NOT NULL)")))

(defn insert-new-user
  [user-name password]
  (let [enc-password (sc/encrypt password 16384 8 1)]
    (j/insert! pg-conn-chess :users
               {:user_name user-name :enc_password enc-password})))

(defn auth-user
  [user-id password]
  (let [enc-password (:enc_password
                       (first
                         (j/query pg-conn-chess
                                  ["SELECT enc_password FROM users WHERE user_id = ? " user-id])))]
    (sc/verify password enc-password)))

(def approved-tokens [{:user1 "Xa&ew"}])

(defn encrypt-token
  [token]
  (sc/encrypt token 16384 8 1))

(defn insert-token
  [token user-id]
  (j/insert! pg-conn-chess :tokens
             {:enc_token token :user_id user-id}))

(defn auth-user-token
  [user-id token]
  (let [resp (first
               (j/query pg-conn-chess ["SELECT * FROM tokens WHERE user_id = ? " user-id]))]
    (= (:enc_token resp) token)))

(defn add-token-user
  []
  (let [token (gen-token)
        ;encrypted-token (encrypt-token token)
        ]
    (insert-token token)
    token))

(defn add-new-video
  [{:keys [url week-num semester-num]}]
  (j/insert! pg-conn-chess :videos
             {:url url :semester_num (Integer/parseInt semester-num) :week_num (Integer/parseInt week-num)})
  (str "the url : " url "is  being added" week-num semester-num))

(defn delete-video
  [id]
  (j/delete! pg-conn-chess :videos
             ["video_id = ? " (Integer/parseInt id)])
  (str "id is being deleted: " id))

(defn accept-post
  [request]
  (def yo request)
  (println "nope"))

(defn accept-heartbeat
  [request]
  (let [token (get (:headers request) "x-token")
        user-id (Integer/parseInt (get (:headers request) "user-id"))]
    (if (auth-user user-id token)
      (str "access approved")
      (str "access denied"))))

(defn accept-test-json
  [request]
  (println "hello tnfewi")
  (println request)
  (println (:body request))
  (println "was")
  ;(println (edn/read (slurp (:body request))))
  #_(println (slurp (:body request)))
  (let [jso (json/parse-string
              (slurp (:body request)) true)]
    (println jso)
    (println (get jso "password")))
  (-> (ra/response (json/generate-string {"hello" "world"}))
      (ra/header "Content-Type" "application/json")))

(defn allow-cross-origin
  [handler]
  (fn [request]
    (let [response (handler request)
          resp
          (-> response
              (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
              (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELETE,OPTIONS")
              (assoc-in [:headers "Access-Control-Allow-Headers"] "X-Requested-With,Content-Type,Cache-Control"))]
      #_(println resp)
      resp)))

(defroutes app-routes
  (GET "/" [] "Hello World")
  #_(GET "/" [] (generate-string {:csrf-token
                                 *anti-forgery-token*}))
  (GET "/add-user" [] (ok page/signin-html))
  (POST "/user" {{user-name "user-name" password "password"} :params}
    (ok (insert-new-user user-name password)))
  (GET "/test" [] "test get")
  (POST "/api/video" [url week-num semester-num]
   (def wn week-num)
   (add-new-video {:url url :week-num week-num :semester-num semester-num}))
  (DELETE "/api/video/:id" [id]
   (delete-video id))
  (POST "/test" [url week] (str url))
  (GET "/api/heartbeat" request (accept-heartbeat request))
  (POST "/api/token" []
   (ok (add-token-user)))
  (ANY "/test-json" request
    (accept-test-json request))
  (route/not-found "Not Found"))

(def app
  (allow-cross-origin
    (params/wrap-params
      app-routes))
  #_(params/wrap-params
      (def/wrap-defaults app-routes def/site-defaults))
  #_(wrap-session
      (def/wrap-defaults app-routes def/site-defaults)
      {:cookie-attrs {:max-age 3600}
       :store        (cookie-store {:key "ahY9poQuaghahc7I"})}))