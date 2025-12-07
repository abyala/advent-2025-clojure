(ns advent-2025-clojure.day07-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day07 :as d]))

(def test-data (slurp "resources/day07-test.txt"))
(def puzzle-data (slurp "resources/day07-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        21 test-data
                        1640 puzzle-data))

(deftest part2-test
  (are [expected input] (= expected (d/part2 input))
                        40 test-data
                        40999072541589 puzzle-data))
