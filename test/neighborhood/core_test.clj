(ns plane.core-test
  (:require [clojure.test :refer :all]
            [plane.core :refer :all]))

(deftest main-test
  (testing "Exit method returns status"
    (is (= 1 (exit 1 "xyzzy")))
    (is (= 0 (exit 0 "xyzzy"))))
  (testing "Sanity checking on the messages"
    (is (string? (usage ["xyzzy"])))
    (is (string? (error-msg ["sdfsdf"]))))
  (testing "Main method gracefully handles input"
    (is (= 0 (-main "build")))
    (is (= 0 (-main "find")))
    (is (= 1 (-main)))
    (is (= 1 (-main "sdfsdf")))
    (is (= 1 (-main "--fakeoption")))))