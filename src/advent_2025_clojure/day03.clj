(ns advent-2025-clojure.day03
  (:require [clojure.string :as str]))

(defn best-joltage [target-len bank]
  (get (reduce (fn [best c]
             (reduce (fn [acc len]
                       (update acc (inc len) (fnil max 0) (parse-long (str c (get acc len "")))))
                     best
                     (range (min (count best) (dec target-len)) -1 -1)))
           {}
           (reverse bank))
       target-len))

(defn solve [target-len input]
  (transduce (map (partial best-joltage target-len)) + (str/split-lines input)))

(defn part1 [input] (solve 2 input))
(defn part2 [input] (solve 12 input))
