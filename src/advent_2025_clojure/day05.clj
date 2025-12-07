(ns advent-2025-clojure.day05
  (:require [abyala.advent-utils-clojure.core :as c]))

(defn parse-input [input]
  (let [[range-str ing-str] (c/split-by-blank-lines input)]
    {:ranges      (partitionv 2 (c/split-longs range-str false))
     :ingredients (c/split-longs ing-str)}))

(defn fresh? [ranges ingredient]
  (some (fn [[low high]] (<= low ingredient high)) ranges))

(defn part1 [input]
  (let [{:keys [ranges ingredients]} (parse-input input)]
    (c/count-when #(fresh? ranges %) ingredients)))

(defn merge-ranges [ranges]
  (reduce (fn [acc [a b]] (let [[_ hi] (last acc)]
                            (if (and hi (<= a (inc hi)))
                              (update-in acc [(dec (count acc)) 1] max b)
                              (conj acc [a b]))))
          []
          (sort ranges)))

(defn part2 [input]
  (c/sum (fn [[low high]] (- high low -1))
         (merge-ranges (:ranges (parse-input input)))))