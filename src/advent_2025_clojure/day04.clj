(ns advent-2025-clojure.day04
  (:require [clojure.set :as set]
            [abyala.advent-utils-clojure.core :as c]
            [abyala.advent-utils-clojure.point :as p]))

(defn parse-rolls [input]
  (set (keep (fn [[k v]] (when (= v \@) k)) (p/parse-to-char-coords input))))

(defn accessible-rolls [rolls]
  (filter #(< (count (set/intersection rolls (set (p/surrounding %)))) 4) rolls))

(defn part1 [input]
  (count (accessible-rolls (parse-rolls input))))

(defn remove-accessible [rolls]
  (if-some [removeable (c/only-when seq (accessible-rolls rolls))]
    (recur (set/difference rolls removeable))
    rolls))

(defn part2 [input]
  (let [rolls (parse-rolls input)]
    (- (count rolls) (count (remove-accessible rolls)))))
