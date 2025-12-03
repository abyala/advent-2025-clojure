(ns advent-2025-clojure.day03-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day03 :as d]))

(def test-data (slurp "resources/day03-test.txt"))
(def puzzle-data (slurp "resources/day03-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        357 test-data
                        17613 puzzle-data))

(deftest part2-test
  (are [expected input] (= expected (d/part2 input))
                        3121910778619 test-data
                        175304218462560 puzzle-data))
