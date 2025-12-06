(ns advent-2025-clojure.day06-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day06 :as d]))

(def test-data (slurp "resources/day06-test.txt"))
(def puzzle-data (slurp "resources/day06-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        4277556 test-data
                        6757749566978 puzzle-data))

(deftest part2-test
  (are [expected input] (= expected (d/part2 input))
                        3263827 test-data
                        10603075273949 puzzle-data))
