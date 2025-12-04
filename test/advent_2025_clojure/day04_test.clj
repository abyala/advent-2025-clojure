(ns advent-2025-clojure.day04-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day04 :as d]))

(def test-data (slurp "resources/day04-test.txt"))
(def puzzle-data (slurp "resources/day04-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        13 test-data
                        1602 puzzle-data))

(deftest part2-test
    (are [expected input] (= expected (d/part2 input))
                          43 test-data
                          9518 puzzle-data))
