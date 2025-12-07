# Day 05: Cafeteria

* [Problem statement](https://adventofcode.com/2025/day/5)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day05.clj)

## Intro

This wasn't necessarily a complicated puzzle, but it took me a while to get it right. Still, I have to say that I
enjoyed it, even though I'm pretty sure we've done similar puzzles in the past. No complaints here, though!

## Expanding the Utils Project

I like reuse, so I expanded one of the existing functions in my
[Advent Utils repo](https://github.com/abyala/advent-utils-clojure) to handle a use case I hadn't considered
before. The `split-longs` function, which I seldom actually use, previously took a string and returned a sequence of
integers it found within the string, including negative values. So `"12a13-14"` would return `(12 13 -14)`. As we'll
see here in a moment, I need this to ignore dashes, so we want that use case to return `(12 13 14)`.

```clojure
(defn split-longs
  "Given an input string, returns a sequence of all numbers extracted, coerced into longs. Any delimiter is acceptable,
  including whitespace, symbols, or any non-numeric character. The function defaults to parsing negative numbers, but
  negations (dashes) can be ignored by setting `include-negatives?` to `false`."
  ([input] (split-longs input true))
  ([input include-negatives?] (map parse-long (re-seq (if include-negatives? #"-?\d+" #"\d+") input))))
```

This function is now multi-arity to avoid breaking the previous contract, and it defaults to including negative values.
But now, the `re-seq` that powers it uses either the `#"-?\d+"` pattern (with negatives) or the simpler `#"\d+"`
pattern if it should ignore dashes.

Back to the puzzle.

## Part One

The first part of this puzzle involved reading a multi-line string with two sections: a list of integer ranges, one per
line, and after a blank line, a list of item ingredient IDs. Our goal is to count the number of ingredients that are
"fresh," meaning that the sit within at least one of the ranges. Oh, and ranges are inclusive on both ends.

Let's start with parsing.

```clojure
(defn parse-input [input]
  (let [[range-str ing-str] (c/split-by-blank-lines input)]
    {:ranges (partition 2 (c/split-longs range-str false))
     :ingredients (c/split-longs ing-str)}))
```

We start with `c/split-by-blank-lines` from my
[core utility namespace](https://github.com/abyala/advent-utils-clojure/blob/main/src/abyala/advent_utils_clojure/core.clj),
which returns a tuple of two strings, one for all of the ranges, and one for all of the ingredients. Ordinarily I would
use the `c/split-blank-line-groups` so that it would return a tuple of sequences of strings, one for each line, but it
was easier to parse each section as a single line. We immediately destructure that tuple into `range-str` and `item-str`
so we can finesse them separately. For the ranges, we want to create a sequence of integer tuples that represents the
low and high for each range. `(c/split-longs range-str false)` finds every non-negative number in the string, and
`(partition 2)` pairs them up. Then for the ingredients, we just need the numbers, so `(c/split-longs ing-str)` is fine,
and we don't need to specify whether or not to allow negatives. Finally, we wrap this all in a nice map of
`{:ranges (...), :ingredients (...)}` for later use.

In the spirit of small, simple functions, let's define `fresh?` to answer whether an ingredient exists within any
range.

```clojure
(defn fresh? [ranges ingredient]
  (some (fn [[low high]] (<= low ingredient high)) ranges))
```

Remember that `some` means "does any value in the collection satisfy the predicate," and is separate from the `some?`
function, which means "is this value not `nil`?" So we just check if any `range` includes the ingredient value. Clojure
makes this pretty in a single function call of `(<= low ingredient high)` since `<=` can handle any positive number of
arguments.

```clojure
(defn part1 [input]
  (let [{:keys [ranges ingredients]} (parse-input input)]
    (c/count-when #(fresh? ranges %) ingredients)))
```

Now it's easy to finish up. We parse the input, and use `c/count-when` to count the number of ingredients that are
fresh within the ranges. Let's move to part 2.

## Expanding the Utils Project Some More

[Fooooled you!](fooledyou.jpeg) First, we need to make more updates to the Utils project so we can keep the `day05`
namespace nice and clean.

First, we make a new `index-of-last` function that parallels the existing `index-of-first` function. It's pretty
simple - we go through the input collection, and whenever the predicate passes, return the index of that value. Then
just select the last index.

```clojure
(defn index-of-last
  "Returns the index of the last value in a collection that returns a truthy response to a predicate filter."
  [pred coll]
  (last (keep-indexed #(when (pred %2) %1) coll)))
```

Then we add two new functions to simplify vector manipulation: `index-at` and `remove-at`. Both of these assume that
we are inserting a single value (how else could we differentiate between "insert all of these" from "insert this
sequence?"), and it doesn't check for an index out of bounds since I can't know what the default behavior should be.
Neither function should need more elaboration, I don't think.

```clojure
(defn insert-at
  "Inserts the value `x` into the existing vector `v` at index `x`, shuffling other items after it. This will throw
  an `IndexOutOfBoundsException` if `idx` is not between 0 and the size of the vector."
  [v idx x]
  (into (subvec v 0 idx) (concat [x] (subvec v idx))))

(defn remove-at
  "Removes a value within the vector `v` at index `idx`, shuffling other items to fill the missing space. This will
  throw an `IndexOutOfBoundsException` if `idx` is not between 0 and the largest index in the vector."
  [v idx]
  (into (subvec v 0 idx) (subvec v (inc idx))))
```

## Part Two

Back to the puzzle. In part 2, we completely ignore the ingredients, and instead need to return the total number of
values that exist within any range at all. It goes without saying that this will be a massive number, so a brute force
method of mapping every range into all of its component values and throwing it all in a giant set won't work.

Everything in the solution comes down to a single function - `merge-range`. The idea is that we will slowly build up
a vector of ranges, ordered by the low value. As we add a new range into this vector, we'll check to see if we can
"merge" it into one or more existing ranges, until we're left with a small vector of non-overlapping ranges. Here we go.

```clojure
(defn merge-range [ranges [a b :as r]]
  (if-some [idx (c/index-of-last #(>= a (first %)) ranges)]                                        ; 1
    (let [[low high] (ranges idx)                                                                  ; 2
          [low' high'] (if (= (inc idx) (count ranges)) [] (ranges (inc idx)))
          a-outside? (> a (inc high))
          merge-next? (and low' (>= b low'))
          b-outside? (> b high)]
      (cond (and a-outside? merge-next?) (recur (c/remove-at ranges (inc idx)) [a (max b high')])  ; 3
            a-outside? (c/insert-at ranges (inc idx) r)
            b-outside? (recur (c/remove-at ranges idx) [(min a low) (max b high)])
            :else (assoc ranges idx [(min a low) (max b high)])))
    (let [[low' high'] (when (seq ranges) (first ranges))]                                         ; 4
      (if (and low' (>= b (dec low')))
        (recur (c/remove-at ranges 0) [a (max b high')])
        (c/insert-at ranges 0 r)))))
```

So much for small, simple functions, but this isn't that bad. Let's break this down.
1. First we check to see if the range we're adding (`r`, composed of `a` and `b`) is greater than other existing ranges
we've already seen. Remember that we're going to keep `ranges` sorted by their low (first) values. We want to know the
*last* index that this new range should follow.
2. If this belongs anywhere but first in line, then we're going to grab some convenience values to make the logic later
easier to read. First we decompose the `low` and `high` value of the last range before this new one and, if it's not
last, the `low'` and `high'` values of the one immediately after it. Then we have some predicates to clarify exactly
where the new range fits with the old. `a-outside?` checks if the new range is completely outside the previous one;
so `[5 7]` is completely outside of `[2 3]`. `merge-next?` checks whether the new range finishes connect to or
overlapping the next range, so `[3 5]` can merge with `[5 7]` or even `[6 7]`. And `b-outside?` checks whether the new
range's high value goes past the previous high one, so even though the low value of `[4 10]` isn't outside of `[3 8]`,
the high value is.
3. Now we do some conditional checks for the typical case where the new range doesn't belong at the front of the ranges.
If the new range doesn't overlap with the previous but does with the next, then we need to squash them together, but
this could cause additional overlaps. So we recurse into the function by removing the next range, and taking the new
low value and whichever high value is greater. If the new range is after the previous but doesn't touch the next one,
then we can just insert it into the vector in its new place without merging. If the new range sits within the previous
and extends further to the right, then we do like the first case - combine them, remove the old, and recurse the
function in case there are more overlaps. Finally, if the new range sits entirely within the old, just merge them.
4. Finally, we handle the case when the new range goes in the front of the vector of ranges, including if it's the
first one. If the new range overlaps with the first one, then we merge and recurse. Otherwise, we just insert it at the
front. 

So it's just a bunch of conditions to check. None of them are very complex, but the key point is that we keep the list
of ranges sorted by its low value, and if we ever merge the new range with an adjacent one then we feed it back through
the function as one big range. It's rather straightforward, especially with the new `c/insert-at` and `c/remove-at`
functions.

```clojure
(defn part2 [input]
  (let [ranges (reduce merge-range [] (:ranges (parse-input input)))]
    (transduce (map (fn [[low high]] (- high low -1))) + ranges)))
```

Now the `part2` function is really simple. We parse the input and pull out just the `:ranges` since we don't care about
the ingredients. We can say `(reduce merge-range [] ranges)` to build the sorted, merged vector of ranges one by one.
And then we can transduce over the ranges, calculating the inclusive sizes of each range.

Wait... didn't I say I like reuse? Let's use our `c/sum` function for `part2` instead!

```clojure
(defn part2 [input]
  (let [ranges (reduce merge-range [] (:ranges (parse-input input)))]
    (c/sum (fn [[low high]] (- high low -1)) ranges)))
```

Under the hood, it's still a call to `transduce`. But since we do so many of these in Advent puzzles, we can just sum
the collection of `ranges` by applying the same subtraction function within. Simple to read.

## Refactoring

After reading [Todd Ginsberg's excellent solution](https://todd.ginsberg.com/post/advent-of-code/2025/day5/) in Kotlin,
I realized I can greatly simplify the `merge-range` logic, eliminating a lot of conditionals, if we simply sort the
sequence of ranges before merging. So let's see what that looks like by replacing `merge-range` with `merge-ranges` and
refactoring `part2` accordingly:

```clojure
(defn merge-ranges [ranges]
  (reduce (fn [acc [a b]] (let [[_ hi] (last acc)]
                            (if (and hi (<= a (inc hi)))
                              (update-in acc [(dec (count acc)) 1] max b)
                              (conj acc [a b]))))
          []
          (sort ranges)))

(defn part2 [input]
  (c/sum (fn [[low high]] (- high low -1))
         (merge-ranges (:ranges (parse-input input)))))
```

`merge-ranges` takes in all ranges from the input and calls `reduce` on them after sorting. Then as we work through
each range, we look to see if the new range's low value (`a`) can be merged with the last accumulated value. If so, we
call `update-in` to set the last accumulated `hi` value to be the max of its old value and the new `b`. If not, we
simply `conj` the new range to the end of the accumulated vector.

With that out of the way, `part1` looks mighty simple. We still use the same `c/sum` function, but we can use a
single expression of `(merge-ranges (:ranges (parse-input input)))` to grab the ranges and merge them.

Thanks, Todd! Nice improvement.