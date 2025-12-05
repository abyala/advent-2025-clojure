(ns advent-2025-clojure.day05
  (:require [abyala.advent-utils-clojure.core :as c]))

(defn parse-input [input]
  (let [[range-str ing-str] (c/split-by-blank-lines input)]
    {:ranges (partition 2 (c/split-longs range-str false))
     :ingredients (c/split-longs ing-str)}))

(defn fresh? [ranges ingredient]
  (some (fn [[low high]] (<= low ingredient high)) ranges))

(defn part1 [input]
  (let [{:keys [ranges ingredients]} (parse-input input)]
    (c/count-when #(fresh? ranges %) ingredients)))

(defn merge-range [ranges [a b :as r]]
  (if-some [idx (c/index-of-last #(>= a (first %)) ranges)]
    (let [[low high] (ranges idx)
          [low' high'] (if (= (inc idx) (count ranges)) [] (ranges (inc idx)))
          a-outside? (> a (inc high))
          merge-next? (and low' (>= b low'))
          b-outside? (> b high)]
      (cond (and a-outside? merge-next?) (recur (c/remove-at ranges (inc idx)) [a (max b high')])
            a-outside? (c/insert-at ranges (inc idx) r)
            b-outside? (recur (c/remove-at ranges idx) [(min a low) (max b high)])
            :else (assoc ranges idx [(min a low) (max b high)])))
    (let [[low' high'] (when (seq ranges) (first ranges))]
      (if (and low' (>= b (dec low')))
        (recur (c/remove-at ranges 0) [a (max b high')])
        (c/insert-at ranges 0 r)))))

(defn part2 [input]
  (let [ranges (reduce merge-range [] (:ranges (parse-input input)))]
    (c/sum (fn [[low high]] (- high low -1)) ranges)))
