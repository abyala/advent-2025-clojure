(ns advent-2025-clojure.day05-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day05 :as d]))

(def test-data (slurp "resources/day05-test.txt"))
(def puzzle-data (slurp "resources/day05-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        3 test-data
                        828 puzzle-data))

(deftest part2-test
  (are [expected input] (= expected (d/part2 input))
                        14 test-data
                        352681648086146 puzzle-data))
