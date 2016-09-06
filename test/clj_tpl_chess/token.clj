(ns clj-tpl-chess.token
  (:import
    [org.apache.commons.lang3 RandomStringUtils]))

(defn gen-token
  []
  (RandomStringUtils/randomAlphabetic 40))