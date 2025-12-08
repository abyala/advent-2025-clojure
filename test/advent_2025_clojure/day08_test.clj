(ns advent-2025-clojure.day08-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day08 :as d]))

(def test-data (slurp "resources/day08-test.txt"))
(def puzzle-data (slurp "resources/day08-puzzle.txt"))

(deftest part1-test
  (are [expected num-connections input] (= expected (d/part1 num-connections input))
                        40 10 test-data
                        67488 1000 puzzle-data))

(deftest part2-test
  (are [expected input] (= expected (d/part2 input))
                        25272 test-data
                        3767453340 puzzle-data))
