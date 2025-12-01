(ns advent-2025-clojure.day01
  (:require [clojure.string :as str]
            [abyala.advent-utils-clojure.core :refer [count-when]]))

(defn num-zeros [from to]
  (+ (if (and (pos-int? from) (not (pos-int? to))) 1 0)
     (quot (abs to) 100)))

(defn rotate [[pos clicks] instruction]
  (let [pos' (({\R + \L -} (get instruction 0)) pos (parse-long (subs instruction 1)))]
    [(mod pos' 100) (+ clicks (num-zeros pos pos'))]))

(defn solve [f input]
  (f (reductions rotate [50 0] (str/split-lines input))))

(defn part1 [input] (solve (partial count-when (comp zero? first)) input))
(defn part2 [input] (solve (comp last last) input))
