(ns plane.build-test
  (:require [clojure.test :refer :all]
            [plane.build :refer :all]))

(def default-options {:inputcoords "data/planes.txt"
                      :outputmap "data/slabs.clj"})

(deftest load-test
  (testing "Get min x and max x of a series of xy points"
    (def test-range (get-xrange [[0M 1M][4M 3M][-6.2M 5M]]))
    (is(= (first test-range) -6.2M))
    (is(= (second test-range) 4M))
    (is(= (count test-range) 2)))
  (testing "Building specific plane"
    (def fake-data '("-85.649292062877,42.9556000817054"
                     "-85.6492705842355,42.9497012172617"
                     "-85.6492578878531,42.948368509832"
                     "-85.645020952015,42.9483342106272"
                     "Next:"))
    (def test-hood(build-hood fake-data nil))
    (is(map? test-hood))
    (is(= (keys test-hood) '(:xs :ys :xrange)))
    (is(> (count(get test-hood :xs)) 0))
    (is(= (get test-hood :xs) '(-85.649292062877M -85.6492705842355M -85.6492578878531M -85.645020952015M)))
    (is(= (get test-hood :ys) '(42.9556000817054M 42.9497012172617M 42.948368509832M 42.9483342106272M))))
  (def real-location "data/gr_planes.txt")
  (def bad-location "nope.jpg")
  (testing "Building collection of hoods from file"
    (is(false? (load-map bad-location)))
    (def test-hoods (load-map real-location))
    (is(map? test-hoods))
    (is(map? (get test-hoods "Roosevelt Park")))))

(deftest slab-test
  (def real-location "data/gr_planes_tinytest.txt")
  (def test-hoods (load-map real-location))
  (testing "Figure out the boundaries for all slabs"
    (def test-walls (build-slab-walls test-hoods))
    (is (coll? test-walls))
    (is (> (count test-walls) 0)))
  (testing "Fetch all line segments in collection that cross slab"
    (def test-edges (get-segment-helper 0M 1M '(0M 0M 1M 1M 0M) '(1M 0M 0M 1M 1M)))
    (is (= (count test-edges)) 2))
  (testing "Checking if hood in slab and organizing its matching segments"
    (def test-segment (get-segment -85.6436602950443M -85.6396729816141M (first test-hoods)))
    (is (= (get test-segment :nick) "East Hills"))
    (is (= 2 (count (get test-segment :lines))))
    (def bad-segment (get-segment 0M -1M (first test-hoods)))
    (is (nil? bad-segment)))
  (testing "Find all hoods that cross a specific slab"
    (def test-slab (find-segments-in-slab -85.6436602950443M -85.6396729816141M test-hoods nil))
    (is (= 2 (count test-slab)))
    (is (= "Baxter" (get (first test-slab) :nick))))
  (testing "Building slabs from a list of their boundaries"
    (def test-slabs (build-slabs nil test-walls test-hoods))
    (is (map? test-slabs))
    (is (= (butlast test-walls) (sort (keys test-slabs))))
    (def first-slab (first test-slabs))
    (is (number? (key first-slab)))))

(deftest overall-test
  (testing "Build function with valid file locs loads hoods, builds slabs, and saves slabs"
    (is (= 0 (build default-options)))
    (is (= 0 (build {:inputcoords "data/gr_planes_tinytest.txt"
                     :outputmap "data/gr_tinytest_slabs.clj"})))))