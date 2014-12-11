(ns plane.query-test
  (:require [clojure.test :refer :all]
            [plane.query :refer :all]))

(def default-options {:inputmap "data/slabs.clj"
                      :output "plane_results.txt"
                      :input "data/query_test.txt"})

(deftest load-slabs-test
  (testing "Loads test-slabs from valid file"
    (def test-slabs (load-slabs (get default-options :inputmap)))
    (is (map? slabs))
    (is (number? (first(keys slabs)))))
  (testing "While loading slabs, gracefully handles bad file locs"
    (is (false? (load-slabs "data/gr_planes.txt")))))

(deftest load-pts-test
  (testing "Succesfully parse x,y points from input string"
    (is (= [-1M, 0M] (parse-lines-helper "xyzzy 1: -1,0")))
    (is (nil? (parse-lines-helper "know where your towel is")))
    (is (nil? (parse-lines-helper "abcdefg 42")))
    (is (nil? (parse-lines-helper ""))))
  (testing "Parse list of points from valid input file"
    (def test-pts (load-pts "data/query_test.txt"))
    (is (list? test-pts))
    (is (= 2 (count (first test-pts))))
    (is (number? (first (first test-pts)))))
  (testing "Gracefully handle bad point input"
    (is (false? (load-pts "nope.jpg")))
    (is (false? (load-pts "data/bad_query_test.txt")))))

(deftest query-pts-test
  (def test-slabs (load-slabs (get default-options :inputmap)))
  (def test-pts (load-pts "data/query_test.txt"))
  (def bounds (apply sorted-set (set (keys slabs))))
  (def test-pt (first test-pts))
  (testing "Find which slab a specific point is in"
    (def slabkey (find-slab (first test-pt) bounds slabs))
    (is (<= slabkey (first test-pt) (get-in test-slabs [slabkey :rbound]))))
  (testing "Get lower ys of a segment"
    (is (coll? (get-segment-bounds (first test-pt) (first (get-in test-slabs [slabkey :segments]))))))
  (testing "Get hash of segments with their ybounds at point x"
    (def test-ordered-segments (get-segments (first test-pt) (get-in test-slabs [slabkey :segments])))
    (is (map? test-ordered-segments))
    (is (< 0 (count (keys test-ordered-segments)))))
  (testing "Correctly find plane for given pt"
    (is (= "Creston" (find-segment test-pt (get-in test-slabs [slabkey :segments])))))
  (testing "Correctly list planes for given points"
    (def test-found-list (find-hoods test-pts slabs))
    (is (= 12 (count test-found-list)))
    (is (= '("ken-O-Sha Park" "Alger Heights" "Southeast Community" "<none>" "John Ball Park"
             "Oldtown-Heartside" "Belknap Lookout" "Heritage Hill" "<none>" "Creston" "North End"
             "Creston")
            test-found-list)))
  (testing "Testing overall load/search query"
    (is (= 0 (query default-options)))))