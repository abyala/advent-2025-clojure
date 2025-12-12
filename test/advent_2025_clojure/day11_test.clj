(ns advent-2025-clojure.day11-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day11 :as d]))

(def test1-data (slurp "resources/day11-test1.txt"))
(def test2-data (slurp "resources/day11-test2.txt"))
(def puzzle-data (slurp "resources/day11-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        5 test1-data
                        566 puzzle-data))

(deftest part2-test
    (are [expected input] (= expected (d/part2 input))
                          2 test2-data
                          331837854931968 puzzle-data))
