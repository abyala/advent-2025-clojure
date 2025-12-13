(ns advent-2025-clojure.day12
  (:require [clojure.string :as str]
            [abyala.advent-utils-clojure.core :as c]))

(defn parse-shape [shape-str]
  (c/count-when #(= % \#) shape-str))

(defn parse-region [region]
  (let [[length width & shapes] (c/split-longs region)]
    {:size (* length width) :shapes-needed shapes}))

(defn parse-input [input]
  (let [[shapes regions] ((juxt butlast last) (c/split-by-blank-lines input))]
    {:shapes (map parse-shape shapes) :regions (map parse-region (str/split-lines regions))}))

(defn fits? [shapes region]
  (let [{:keys [size shapes-needed]} region]
    (<= (c/sum (map * shapes shapes-needed)) size)))

(defn part1 [input]
  (let [{:keys [shapes regions]} (parse-input input)]
    (c/count-when (partial fits? shapes) regions)))
