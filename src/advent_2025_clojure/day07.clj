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

(defn count-paths [grid beam]
  (letfn [(move-down [beams]
            (if-some [results (seq (mapcat (fn [[b n]] (map #(hash-map % n) (points-below grid b)))
                                           beams))]
              (recur (apply merge-with + results))
              (c/sum second beams)))]
    (move-down {beam 1} )))

(defn solve [path-fn input]
  (let [grids (p/parse-to-char-coords-map input)
        start (first (c/first-when #(= \S (second %)) grids))]
    (path-fn grids start)))

(defn part1 [input] (solve count-splits input))
(defn part2 [input] (solve count-paths input))