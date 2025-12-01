(ns advent-2025-clojure.day01-test
  (:require [clojure.test :refer :all]
            [advent-2025-clojure.day01 :as d]))

(def puzzle-data (slurp "resources/day01-puzzle.txt"))

(deftest part1-test
  (are [expected input] (= expected (d/part1 input))
                        3 "L68\nL30\nR48\nL5\nR60\nL55\nL1\nL99\nR14\nL82"
                        1011 puzzle-data))

(deftest rotate-test
  (testing "From 50"
    (are [pos' clicks' instruction] (= [pos' clicks'] (d/rotate [50 0] instruction))
                                    0 1 "L50"
                                    0 1 "R50"
                                    1 0 "L49"
                                    99 0 "R49"
                                    99 1 "L51"
                                    1 1 "R51"))
  (testing "From 0"
    (are [pos' clicks' instruction] (= [pos' clicks'] (d/rotate [0 0] instruction))
                                    50 0 "L50"
                                    50 0 "R50"
                                    51 0 "L49"
                                    49 0 "R49"
                                    49 0 "L51"
                                    51 0 "R51")))

(deftest part3-test
  (are [expected input] (= expected (d/part2 input))
                        6 "L68\nL30\nR48\nL5\nR60\nL55\nL1\nL99\nR14\nL82"
                        5937 puzzle-data))