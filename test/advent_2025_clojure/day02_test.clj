(ns advent-2025-clojure.day02-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day02 :as d]))

(def test-data "11-22,95-115,998-1012,1188511880-1188511890,222220-222224,\n1698522-1698528,446443-446449,38593856-38593862,565653-565659,\n824824821-824824827,2121212118-2121212124")
(def puzzle-data (slurp "resources/day02-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        1227775554 test-data
                        18952700150 puzzle-data))

(deftest part2-test
  (are [expected input] (= expected (d/part2 input))
                        4174379265 test-data
                        28858486244 puzzle-data))

