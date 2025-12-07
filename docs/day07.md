# Day 07: Laboratories

* [Problem statement](https://adventofcode.com/2025/day/7)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day07.clj)

## Intro

I do enjoy Advent puzzles where the performance issue from going brute force shows up a mile away! Maybe I've just been
doing these puzzles for too long.

## Part One

We are given grid for our "teleportation hub," containing a single tachyon beam starting location (denoted by `"S"`),
and some splitters (denoted by `"^"`). The beam always travels down, but when it hits a splitter, it replaces itself
with beams on either side that also only travel down. Our goal is to count the number of times a beam splits. Note that
two beams on the same space merge into one, so we only look at distinct beams at a time.

Normally we start with parsing, but we're going to use our old library function again, so instead we'll start with a
helper function `points-below`, which tells us where a beam will end up the next time it moves down.

```clojure
(defn points-below [grid beam]
  (let [beam' (p/move beam [0 1])]
    (case (grid beam')
      \. [beam']
      \^ (map (partial p/move beam') [[-1 0] [1 0]])
      nil nil)))
```

Given the parsed `grid`, which will take the form `{[x y] c}`, and the `beam` of form `[x y]`, we find the coordinates
directly below `beam`. One of my big regrets when making my 
[point utility namespace](https://github.com/abyala/advent-utils-clojure/blob/main/src/abyala/advent_utils_clojure/point.clj)
is that I didn't make it clear between grids that have `y=0` at the top and at the bottom/middle, so `down` or `south`
are ambiguous. As a result, I just put the direction in manually. So anyway, we move the beam down one space with
`(p/move beam [0 1])` (since `y` increases moving down), and then we check what's in the `grid` at that location. When
it's a space, then the collection of new beam locations contains only one point. When it's a splitter, then we return
two new spaces by moving the beam left and right (`[-1 0]` and `[1 0]`, respectively). And when it's `nil`, we've hit 
the bottom of the grid so the beam stops.

Now let's implement `count-splits` to see how many times the beam splits along its journey.

```clojure
(defn count-splits [grid beam]
  (letfn [(move-down [beams n]
            (if-some [results (seq (keep #(points-below grid %) beams))]
              (recur (set (apply concat results)) (+ n (c/count-when #(= (count %) 2) results)))
              n))]
    (move-down [beam] 0)))
```

This function takes in the `grid` and the initial `beam`, but it conceals a private/nested function called `move-down`.
The caller shouldn't know about that function, so I don't expose it on the namespace level. `move-down` takes in a
sequence of beams and the number of splits already seen. It calls `points-below` on its `beams` argument, using `keep`
to remove the `nil`s and then checking to see if it's an empty sequence using `seq`; either the points will all be in
the grid or `nil`, since everything moves down at the same time. If we find values for the next row, then we recursively
call `move-down` again - the new collection of beams comes from `(set (apply concat results))` to collect unique beam
locations, and the new number of splits adds the previous sum to the number of results of size 2.

Let's wrap up part 1.

```clojure
(defn part1 [input]
  (let [grid (p/parse-to-char-coords-map input)
        start (first (c/first-when #(= \S (second %)) grid))]
    (count-splits grid start)))
```

We parse the `grid` using our old friend `p/parse-to-char-coords-map`, and we find the starting point by using
`(c/first-when #(= \S (second %)) grid)`, remembering that the `grid` is a sequence of `[[x y] c]` pairs, and we need
to look at the second value (`c`). We call `first` once more since `c/first-when` will return the `[[x y] c]` tuple
and we need the coordinates. Then we just need to call `count-splits` to get our answer.

## Part Two

So now we need to calculate the number of distinct paths the beam can take across every splitter. The brute force
solution would be to do a simple breadth-first search, but that will spiral out too far. Luckily, we can keep that
simplistic algorithm and use a little memoization to keep the performance super fast.

It's all really just one simple function - `count-paths`.

```clojure
(def count-paths
  (memoize (fn [grid beam]
             (if-some [below (points-below grid beam)]
               (c/sum (partial count-paths grid) below)
               1))))
```

The first two lines essentially say that `count-paths` is a function with the same arguments as `count-splits`, but
Clojure will internally cache the result of every unique pair of arguments instead of recalculating them. It's nice
that we don't have to do this caching ourselves! We again call `points-below` on the `beam` value (it's not a sequence)
to see if the beam can still travel. If so, we'll use `c/sum` to call `count-paths` for each point below and add them
together. If any call to this function results in no points `below` the argument, then that's a single path down, and
it returns 1.

We're going to skip over the naive `part2` function and create the unified `solve` function right away again.

```clojure
(defn solve [path-fn input]
  (let [grids (p/parse-to-char-coords-map input)
        start (first (c/first-when #(= \S (second %)) grids))]
    (path-fn grids start)))

(defn part1 [input] (solve count-splits input))
(defn part2 [input] (solve count-paths input))
```

The `solve` function looks identical to the old `part1` function, except that once we parse the `grid` and find the
`start` position, we parameterize which path function to apply. `part1` uses `count-splits` and `part2` uses
`count-paths`. And that's it!
