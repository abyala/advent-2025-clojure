(ns advent-2025-clojure.day07
  (:require [abyala.advent-utils-clojure.core :as c]
            [abyala.advent-utils-clojure.point :as p]))

(defn points-below [grid beam]
  (let [beam' (p/move beam [0 1])]
    (case (grid beam')
      \. [beam']
      \^ (map (partial p/move beam') [[-1 0] [1 0]])
      nil nil)))

(defn count-splits [grid beam]
  (letfn [(move-down [beams n]
            (if-some [results (seq (keep #(points-below grid %) beams))]
              (recur (set (apply concat results)) (+ n (c/count-when #(= (count %) 2) results)))
              n))]
    (move-down [beam] 0)))

(def count-paths
  (memoize (fn [grid beam]
             (if-some [below (points-below grid beam)]
               (c/sum (partial count-paths grid) below)
               1))))

(defn solve [path-fn input]
  (let [grids (p/parse-to-char-coords-map input)
        start (first (c/first-when #(= \S (second %)) grids))]
    (path-fn grids start)))

(defn part1 [input] (solve count-splits input))
(defn part2 [input] (solve count-paths input))