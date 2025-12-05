(ns advent-2025-clojure.day04
  (:require [abyala.advent-utils-clojure.core :as c]
            [abyala.advent-utils-clojure.point :as p]))

(defn parse-rolls [input]
  (set (keep (fn [[k v]] (when (= v \@) k)) (p/parse-to-char-coords input))))

(defn accessible? [rolls p]
  (< (c/count-when rolls (set (p/surrounding p))) 4))

(defn prune-accessible [remove-all? rolls]
  (let [remaining (set (remove (partial accessible? rolls) rolls))]
    (if (and remove-all? (not= rolls remaining))
      (recur remove-all? remaining)
      remaining)))

(defn solve [remove-all? input]
  (let [rolls (parse-rolls input)]
    (- (count rolls) (count (prune-accessible remove-all? rolls)))))

(defn part1 [input] (solve false input))
(defn part2 [input] (solve true input))
