(ns clj-tpl-chess.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-tpl-chess.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/test"))]
      (is (= (:status response) 200))
      (is (= (:body response) "test get"))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

(deftest test-video
  (testing "main route"
    (let [response (app (mock/request :post "/api/video"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World")))))
