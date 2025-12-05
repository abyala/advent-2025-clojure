# Day 04: Printing Department

* [Problem statement](https://adventofcode.com/2025/day/4)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day04.clj)

## Intro

This was one of the simplest puzzles we've had in a while; no complaints! I'm not even planning on making a unified
`solve` function this time since it's not worth it. But instead, I'll talk a little bit about my library of utility
functions today.

## Part One

In this puzzle, we're given a grid on which we find rolls of paper, displayed as an `@` instead of an empty space as a
period. We need to count the number of rolls of paper that are accessible by a forklift, which happens if they are
surrounded by fewer than 4 other rolls of paper.

To start, let's parse the input file into a set of coordinates for the rolls of paper.

```clojure
(defn parse-rolls [input]
  (set (keep (fn [[k v]] (when (= v \@) k)) (p/parse-to-char-coords input))))
```

We start with my [point utility library](https://github.com/abyala/advent-utils-clojure/blob/main/src/abyala/advent_utils_clojure/point.clj)
in which we find `p/parse-to-char-coords` function that takes in a grid as a string and returns a sequence of 
`[[x y] c]` tuples, starting with the coordinates and ending with the character value. We then use `keep` to filter for
values that are rolls of paper, returning just the coordinate key, and then turn it all into a set. In most Advent
puzzles, I tend to use `p/parse-to-char-coords-map`, which returns a map of `{[x y] c}` instead. Fun fact - the
`parse-rolls` function would not have to change in any way if we used that function instead! You can still use standard
library functions of `map`, `filter`, `keep`, etc. over a map, and it interprets the key-value pairs as a tuple.
Finally, everything today will leverage sets, so we wrap the filtered sequence into one with the `set` function.

Next, we implement `accessible-rolls`, which takes in a set of rolls and returns the ones with fewer than 4 surrounding
neighbors.

```clojure
(defn accessible-rolls [rolls]
  (filter #(< (count (set/intersection rolls (set (p/surrounding %)))) 4) rolls))
```

This is just a simple `filter` function over the set of rolls. We reuse the `p/surrounding` function from the `points`
utility namespace again, and that takes in an `[x y]` coordinate pair and returns the eight coordinates around it. With
those points as a set, we can call `set/intersection` on `rolls` to check which surrounding points are in fact rolls of
paper, and compare that count to 4.

Let's wrap it up.

```clojure
(defn part1 [input]
  (count (accessible-rolls (parse-rolls input))))
```

Super simple - parse the input into the set of rolls, find the accessible ones, and count them up.

## Part Two ##

Now we have to keep removing accessible rolls until there are no more that can be removed, and count how many were
discarded. Let's not over-complicate things.

```clojure
(defn remove-accessible [rolls]
  (if-some [removeable (c/only-when seq (accessible-rolls rolls))]
    (recur (set/difference rolls removeable))
    rolls))
```

The `remove-accessible` function is recursive, as we Lisp folks like to be. Given a set of rolls, we call
`acceessible-rolls` to see if any can be removed. I really love my `c/only-when` function, which returns its function
argument if a predicate is true, or else `nil` if not. This allows us to avoid making a `let` statement, then a nested
`if`, and it keeps things nice and concise. If we find any rolls to remove, use `set/difference` to take them out of
the current set of `rolls`, and then recursively call the function with the whittled-down set. Otherwise, we're done so
return the remaining set.

```clojure
(defn part2 [input]
  (let [rolls (parse-rolls input)]
    (- (count rolls) (count (remove-accessible rolls)))))
```

Finally, we solve part 2. After parsing the input, return the count of the full set of rolls, minus the count of rolls
after removing the accessible ones.

## Refactored

Ok, I lied. I like having a single `solve` function and there were things I wanted to clean up. So let's refactor.

First of all, I didn't like how `accessible-rolls` did two things - check if a roll was accessible for the forklift,
and removed it from the set. So let's make a nice `accessible?` function.

```clojure
(defn accessible? [rolls p]
  (< (c/count-when rolls (set (p/surrounding p))) 4))
```

So this is nice and easy to understand: take the set of points around a point `p`, and return whether the number of
them in the set of rolls is below 4. Bonus - we get to use `count-when` again!

Next, let's make a function `prune-accessible` to replace the old `accessible-rolls` function.

```clojure
(defn prune-accessible [remove-all? rolls]
  (let [remaining (set (remove (partial accessible? rolls) rolls))]
    (if (and remove-all? (not= rolls remaining))
      (recur remove-all? remaining)
      remaining)))
```

This time, we use the `remove` function over all of the accessible rolls passed in, and we introduce a function
parameter `remove-all?` to state whether to remove rolls once or recursively. We recurse back through the function if
the parameter says to do so and if the set of `rolls` is different from the set of `remaining` rolls after getting rid
of the accessible ones. If we don't recurse or the set didn't change, return the remaining rolls.

Now we can make our common function.

```clojure
(defn solve [remove-all? input]
  (let [rolls (parse-rolls input)]
    (- (count rolls) (count (prune-accessible remove-all? rolls)))))

(defn part1 [input] (solve false input))
(defn part2 [input] (solve true input))
```

`solve` pretty much does what the old `part2` did - parse the rolls, and return the difference between the original
count and the number of ones leftover after pruning. `part1` calls it with `false` for `remove-all?` and `part2` calls
it with `true`. Granted, we don't _really_ need to do that subtraction for part 1, and we could have structured the
common function to return the number of rolls removed (I originally did that), but these functions seem more intuitive
to me.

Ahhhhh... I feel better now.
