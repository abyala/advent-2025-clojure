(ns advent-2025-clojure.day09-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day09 :as d]))

(def test-data (slurp "resources/day09-test.txt"))
(def puzzle-data (slurp "resources/day09-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        50 test-data
                        4782151432 puzzle-data))

(deftest part2-test
    (are [expected input] (= expected (d/part2 input))
                          24 test-data
                          1450414119 puzzle-data))
