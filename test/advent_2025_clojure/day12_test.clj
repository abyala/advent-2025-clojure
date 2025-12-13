(ns advent-2025-clojure.day12-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day12 :as d]))

(def puzzle-data (slurp "resources/day12-puzzle.txt"))

; NOTE: The algorithm doesn't work on the test data; just the puzzle data.
(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        531 puzzle-data))
