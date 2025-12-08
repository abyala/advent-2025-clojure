(ns advent-2025-clojure.day08
  (:require [clojure.math :as math]
            [clojure.set :as set]
            [abyala.advent-utils-clojure.core :as c]))

(defn parse-boxes [input]
  (partition 3 (c/split-longs input)))

(defn straight-line-distance [p0 p1]
  (math/sqrt (c/sum (map #(math/pow (- %1 %2) 2) p0 p1))))

(defn shortest-connections-seq [boxes]
  (->> (c/unique-combinations boxes)
       (map (fn [[a b]] {:circuits [a b] :distance (straight-line-distance a b)}))
       (sort-by :distance)
       (map :circuits)))

(defn connect-boxes-seq [boxes]
  (letfn [(connect-next [circuits [[b0 b1] & next-conns]]
            (when b0
              (let [idx0 (c/index-of-first #(% b0) circuits)
                    idx1 (c/index-of-first #(% b1) circuits)
                    circuits' (if (= idx0 idx1)
                                circuits
                                (-> circuits
                                    (update idx0 set/union (circuits idx1))
                                    (c/remove-at idx1)))]
                (cons {:circuits circuits' :last-connection [b0 b1]}
                      (lazy-seq (connect-next circuits' next-conns))))))]
    (let [init-circuits (mapv hash-set boxes)]
      (cons {:circuits init-circuits :last-connection []}
            (lazy-seq (connect-next init-circuits (shortest-connections-seq boxes)))))))

(defn part1 [num-connections input]
  (->> (connect-boxes-seq (parse-boxes input))
       (#(nth % num-connections))
       :circuits
       (map count)
       sort
       (take-last 3)
       c/product))

(defn part2 [input]
  (->> (connect-boxes-seq (parse-boxes input))
       (c/first-some (fn [{:keys [circuits last-connection]}] (when (= (count circuits) 1) last-connection)))
       (c/product first)))
