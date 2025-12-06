(ns advent-2025-clojure.day06
  (:require [clojure.string :as str]
            [abyala.advent-utils-clojure.core :as c]))

(defn parse-columns [lines]
  (apply map list (map c/split-longs lines)))

(defn calculate [nums op]
  (apply ({"*" * "+" +} op) nums))

(defn parse-blankable-long [coll]
  (parse-long (str/trim (apply str coll))))

(defn pad-equal [lines]
  (let [pattern (str "%-" (apply max (map count lines)) "s")]
    (map #(format pattern %) lines)))

(defn parse-cephalopod-columns [lines]
  (let [padded (pad-equal lines)]
    (->> (apply interleave padded)
         (partition-all (count padded))
         (map parse-blankable-long)
         (partition-by nil?)
         (take-nth 2))))

(defn solve [parse-fn input]
  (let [[num-str op-str] ((juxt butlast last) (str/split-lines input))]
    (apply + (map calculate (parse-fn num-str) (re-seq #"\S" op-str)))))

(defn part1 [input] (solve parse-columns input))
(defn part2 [input] (solve parse-cephalopod-columns input))
