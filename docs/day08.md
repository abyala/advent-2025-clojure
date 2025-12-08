# Day 08: Playground

* [Problem statement](https://adventofcode.com/2025/day/8)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day08.clj)

## Intro

This was a cute little puzzle where I got to play around with recursion, lazy sequences, and even some exciting
destructuring! Fascinated? Of course you are.

## Part One

We are given an input file with multiple junction boxes in 3-dimensional space, in the form `x,y,z`, and we need to
start connecting them in order of shortest distances between any two pairs. Then after 10 connections (test) or
1000 connections (main puzzle), we need to multiply the sizes of the three largest circuits of connected boxes.

Parsing shouldn't require any explanation from here.

```clojure
(defn parse-boxes [input]
  (partition 3 (c/split-longs input)))
```

To calculate distance, we need to use "straight line distance," which involves taking the square root of the sum of
squaring the differences of all ordinates between points. In this case, that's the square root of
`(x0-x1)^2 + (y0-y1)^2 + (z0-z1)^2`. Easy enough to convert that to Clojure.

```clojure
(defn straight-line-distance [p0 p1]
  (math/sqrt (c/sum (map #(math/pow (- %1 %2) 2) p0 p1))))
```

As seems to be the theme this year, we're going to use `map` over multiple collections, in this case the three
ordinates from points `p0` and `p1`. We'll match each pair using `(math/pow (- %1 %2) 2)`, then sum them and take the
final square root. Note that we are using the `clojure.math` namespace for `math/pow` and `math/pow` functions, but
they just wrap Java's static methods in the Math class.

Now let's create the sequence of connections between boxes, from shortest to longest.

```clojure
(defn shortest-connections-seq [boxes]
  (->> (c/unique-combinations boxes)
       (map (fn [[a b]] {:circuits [a b] :distance (straight-line-distance a b)}))
       (sort-by :distance)
       (map :circuits)))
```

Ok, we're cheating - I already created the `unique-combinations` function in my 
[core utilities namespace](https://github.com/abyala/advent-utils-clojure/blob/main/src/abyala/advent_utils_clojure/core.clj).
This function just does a `for`, which uses list comprehension to map each box to each other box by index. Given 
`(c/unique-combinations [10 20 30])`, it returns `([10 20] [10 30] [20 30])`, which is just what we need. Then we
calculate the distance between each pair, forming a box of `{:circuits [a b], :distance n}` before sorting by the
distance and returning the circuits. I was a little surprised that I had to make these maps and rip them apart again,
but if I just did `(sort-by (partial apply straight-line-distance) (c/unique-combinations boxes))`, it took 8x as long.
I guess it's constantly recalculating the distances every time it does the comparison, and that adds up.

Now that we've got our best connections, let's create a sequence of the state of the circuits before each new
connection.

```clojure
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
                (cons circuits (lazy-seq (connect-next circuits' next-conns))))))]
    (connect-next (mapv hash-set boxes) (shortest-connections-seq boxes))))
```

Let's look at the bottom of this function first. Based on an inner function called `connect-next`, we call it with
two arguments - `(mapv hash-set boxes)`, which turns the sequence of boxes into a vector of sets that each contain
one box, and `(shortest-connections-seq boxes)` which is our sequence of pairs of boxes to connect. The bulk of the
work happens in `connect-next`. Right off the bat, we destructure the connection sequence into `[[b0 b1] & next-conns]`
to make it easier to use. `[b0 b1]` is the first element of the sequence, which we destructure further into the two
boxes by IDs `b0` and `b1`, and `& next-conns` is effectively the `tail` of the sequence, or everything after the first.

We use `b0` as our escape clause - if the sequence were finished, the first element would be `nil`, and both `b0` and
`b1` would be `nil`, so the `when` clause would return `nil` too. Otherwise, we need to decide what happens with the
next connection. `idx0` and `idx1` use `c/index-of-first` to find which circuit (set) contains each box, and then we
define `circuits'` for the next state after combining them. If the two boxes are already in the same circuit,
`(= idx0 idx1)`, then the next state is the current state, so we reuse `circuits`. If not, we need to add all boxes
from the second circuit into the first with `(update circuits idx0 set/union (circuits idx1))`, and then remove the old
second circuit. Thanks to the work we did on day 5, we now have a utility function to make
`(c/remove-at circuits idx1)`. Finally, we return a sequence of the current state of `circuits`, and a lazy recursive
call back in to `connect-next` with `circuits'` and `next-conns`.

Given that function, `part1` is fairly trivial to write.

```clojure
(defn part1 [num-connections input]
  (apply * (take-last 3 (sort (map count (nth (connect-boxes-seq (parse-boxes input)) num-connections))))))
```

Note that this function takes in two parameters - the number of connections we want to apply before the calculation
(10 for the example data and 1000 for the puzzle data), and the raw input. Breaking down the function: we parse the
input into boxes, turn them into the sequence of circuit states, use `nth` to find the state number needed, then `map`
each circuit into its size, sort them, and call `(take-last 3 sorted-counts)` to get the three largest. Finally,
`(apply * (three-largest-sizes))` multiplies them together to get to our answer.

## Part Two

For part two, we need to multiply together the last two boxes that we connected to result in a single circuit with all
boxes connected. For this, we'll need to modify `connect-boxes-seq`, such that each value in the sequence contains both
the circuits and the two boxes connected to get there. We'll represent this as
`{:circuits cs, :last-connection [b0 b1]}`.

```clojure
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
```

Let's again start with the end of this function to see how we invoke the inner function `connect-next`. We'll again
call `(mapv hash-set boxes)`, but now we do the recursive call by constructing our map with the initial list of
circuits and an empty collection of boxes, since we connected nothing to get to the starting state. Technically, we
could have avoided this and used some offset magic later, but it made sense to me that the first element of the
sequence should represent the starting state.

The rest of the logic is the same, except that the return expression also calls `(cons state-map (lazy-seq next-call))`.

Now we can refactor `part1` and implement `part2`:

```clojure
(defn part1 [num-connections input]
  (->> (connect-boxes-seq (parse-boxes input))
       (#(nth % num-connections))
       :circuits
       (map count)
       sort
       (take-last 3)
       (apply *)))

(defn part2 [input]
  (->> (connect-boxes-seq (parse-boxes input))
       (c/first-when #(= (count (:circuits %)) 1))
       :last-connection
       (map first)
       (apply *)))
```

The only difference in `part1` is that it now calls `:circuits` after `nth` to pull out the circuits from the return
value of `connect-boxes-seq`, but I also put in a `->>` thread-last function to make it easier to read. For `part2`,
we parse and make the sequence, use `c/first-when` to find the first state with a single circuit, grabbing its
connection. Then `(map first connection)` pulls out the `x` ordinates from both boxes, and `(apply * xs)` multiplies
them.

There's no real reason to do a unified `solve` function - all it would do is parse and make the sequence of states, but
that's not worth it. So we're done!

## Refactoring

While cleaning up the code for the write-up, I realized I wanted to make a few more functions in the
[core utilities namespace](https://github.com/abyala/advent-utils-clojure/blob/main/src/abyala/advent_utils_clojure/core.clj)
because, well, I can.  So here are the two tiny little functions.

```clojure
(defn first-some
  "Returns the first non-nil value from applying the function `f` to a collection."
  [f coll]
  (first (keep f coll)))

(defn product
  "Multiplies the values in a collection. If a function `f` is provided, then map `f` to each value in the collection
  before multiplying them together."
  ([coll] (apply * coll))
  ([f coll] (transduce (map f) * coll)))
```

`first-some` looks much like `first-when`, except that instead of using a predicate, it just returns the first non-nil
value from a collection when applying a function `f` to it. `keep` already applies a function to a collection and
discards `nil` values, so `first-some` just wraps that with `first`.

I kind of wish I would name `product` something like `*` or even `**`, but I already have a function called `sum` so I'd
rather be consistent. This looks exactly the same as a the `sum` function, except it does multiplication over a
collection, optionally using a transformation function.

What does this change for us? Not all that much!

```clojure
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
```

In `part1`, we can replace the final `(apply *)` with `c/product`. And in `part2`, we can replace calling `c/first-when`
and `:last-connections` with a single call to `first-some`. We also replace the combination of `(map first)` and
`(apply *)` with a single `(c/product first)`.
