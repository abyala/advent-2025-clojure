(ns advent-2025-clojure.day10
  (:require [clojure.string :as str]
            [abyala.advent-utils-clojure.core :as c])
  (:import (clojure.lang PersistentQueue)))

(defn parse-machine [s]
  (let [[_ diagram schematic-str _] (re-find #"\[([^\]]+)\] ([^{]+) (.*)" s)]
    {:diagram    (mapv (partial = \#) diagram),
     :schematics (set (map c/split-longs (str/split schematic-str #" ")))
     :joltages   (vec (c/split-longs (last (re-find #"\[([^\]]+)\] ([^{]+) (.*)" s))))}))

(defn push-buttons [lights schematic]
  (reduce #(update %1 %2 not) lights schematic))

(defn wire-up [{:keys [diagram schematics]}]
  (loop [queue (conj PersistentQueue/EMPTY [(vec (repeat (count diagram) false)) schematics])
         seen #{}]
    (let [[lights unused :as option] (peek queue)]
      (cond (= lights diagram) (- (count schematics) (count unused))
            (seen option) (recur (pop queue) seen)
            :else (recur (reduce conj (pop queue) (map #(vector (push-buttons lights %) (disj unused %)) unused))
                         (conj seen option))))))

(defn part1 [input]
  (c/sum wire-up (map parse-machine (str/split-lines input))))
