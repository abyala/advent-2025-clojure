(ns advent-2025-clojure.day10-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day10 :as d]))

(def test-data (slurp "resources/day10-test.txt"))
(def puzzle-data (slurp "resources/day10-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        7 test-data
                        455 puzzle-data))

; I did not implement part 2 of this pointless puzzle.
#_(deftest part2-test
  (are [expected input] (= expected (d/part2 input))
                        33 test-data
                        16978 puzzle-data))
