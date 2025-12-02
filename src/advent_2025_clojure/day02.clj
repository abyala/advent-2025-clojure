(ns advent-2025-clojure.day02
  (:require [abyala.advent-utils-clojure.core :refer [repeat-string]]))

(defn parse-input [input]
  (partition 2 (map parse-long (re-seq #"\d+" input))))

(defn invalid? [only-doubles? n]
  (when (> n 9)
    (let [s (str n)
          len (count s)
          pivot (/ len 2)
          lengths (if only-doubles? [pivot] (range 1 (inc pivot)))]
      (some #(= s (repeat-string (/ len %) (subs s 0 %))) lengths))))

(defn invalid-ids [only-doubles? [low high]]
  (filter (partial invalid? only-doubles?) (range low (inc high))))

(defn solve [only-doubles? input]
  (apply + (mapcat (partial invalid-ids only-doubles?) (parse-input input))))

(defn part1 [input] (solve true input))
(defn part2 [input] (solve false input))
