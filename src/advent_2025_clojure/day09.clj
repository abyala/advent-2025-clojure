(ns advent-2025-clojure.day09
  (:require [abyala.advent-utils-clojure.core :as c]
            [abyala.advent-utils-clojure.point :as p]))

(defn parse-points [input] (partitionv 2 (c/split-longs input)))

(defn rectangle-area [[x0 y0] [x1 y1]]
  (* (inc (abs (- x0 x1)))
     (inc (abs (- y0 y1)))))

(defn part1 [input]
  (apply max (map (partial apply rectangle-area)
                  (c/unique-combinations (parse-points input)))))

(defn follow-path [points]
  (let [first-point (first (sort points))]
    (loop [path [first-point], search (disj (set points) first-point), horizontal? true]
      (if (seq search)
        (let [dir-fn (if horizontal? first second)
              path-ord (dir-fn (last path))
              p' (c/first-when #(= path-ord (dir-fn %)) search)]
          (recur (conj path p') (disj search p') (not horizontal?)))
        (conj path (first path))))))

(defn row-gaps [min-x max-x segments row]
  (->> (reduce (fn [[in-gap? found] [[x0] [x1]]]
                 [(not in-gap?) (if in-gap? (assoc-in found [(dec (count found)) 1] (dec x0))
                                            (conj found [(inc x1) max-x]))])
               [true [[min-x max-x]]]
               (filter (fn [[[_ y0] [_ y1]]] ((if (> y0 y1) >= <=) y0 row y1)) segments))
       second
       (remove (partial apply >=))))

(defn gap-groups [points]
  (let [path (follow-path points)
        segments (->> (partition 2 1 path)
                      (map (partial sort-by (juxt first second)))
                      (sort-by (juxt first second)))
        [[min-x min-y] [max-x max-y]] (p/bounding-box points)]
    (first (reduce (fn [[gaps last-gap :as acc] row]
                     (let [gap (seq (row-gaps min-x max-x segments row))]
                       (cond (nil? gap) (assoc acc 1 nil)
                             (= gap last-gap) (update-in acc [0 (dec (count gaps)) 0 1] inc)
                             :else (-> acc
                                       (update 0 conj [[row row] gap])
                                       (assoc 1 gap)))))
                   [[] nil]
                   (range min-y (inc max-y))))))

(defn has-gap? [gap-groups [[x0 y0] [x1 y1]]]
  (let [[y-min y-max] (apply (juxt min max) [y0 y1])
        gap-columns (->> gap-groups
                         (drop-while (fn [[[_ y]]] (< y y-min)))
                         (take-while (fn [[[_ y]]] (<= y y-max)))
                         (mapcat second))]
    (some (fn [[low high]] (or (<= x0 low x1) (<= low x0 high))) gap-columns)))

(defn part2 [input]
  (let [points (parse-points input)
        gap-groups (gap-groups points)]
    (->> (sort-by (juxt first second) points)
         (c/unique-combinations)
         (remove (fn [[[x0 y0] [x1 y1]]] (or (= x0 x1) (= y0 y1))))
         (remove (partial has-gap? gap-groups))
         (map (partial apply rectangle-area))
         (apply max))))
