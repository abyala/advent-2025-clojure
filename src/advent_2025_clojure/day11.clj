(ns advent-2025-clojure.day11
  (:require [clojure.string :as str]
            [abyala.advent-utils-clojure.core :as c]))

(defn parse-input [input]
  (reduce #(let [[from & to] (re-seq #"\w+" %2)]
             (assoc %1 from (set to)))
          {}
          (str/split-lines input)))

(defn merge-child-paths [p0 p1 from]
  (let [add-over (fn [p [from to]] (-> p
                                     (update to + (from p))
                                     (assoc from 0)))
        p' (merge-with + p0 p1)]
    (case from "dac" (reduce add-over p' [[:neither :dac] [:fft :both]])
               "fft" (reduce add-over p' [[:neither :fft] [:dac :both]])
               p')))

(def no-paths (zipmap [:neither :dac :fft :both] (repeat 0)))
(defn paths-to-out [outbounds from]
  (letfn [(nested-paths [dev seen]
            (if (seen dev) seen
                           (reduce (fn [acc to] (let [{p' to :as acc'} (nested-paths to acc)]
                                                  (update acc' dev merge-child-paths p' dev)))
                                   (assoc seen dev no-paths)
                                   (outbounds dev))))]
    ((nested-paths from {"out" (assoc no-paths :neither 1)}) from)))

(defn part1 [input]
    (c/sum (vals (paths-to-out (parse-input input) "you"))))

(defn part2 [input]
  (:both (paths-to-out (parse-input input) "svr")))
