# Day 09: Movie Theater

* [Problem statement](https://adventofcode.com/2025/day/9)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day09.clj)

## Intro

I'm going to say this right off the bat - this one took me a while to get right, and then a while longer to run in a
reasonable amount of time. I suspect that there are clever solutions out there, but I want to publish my solution, as
verbose as it is, first. Oh, there's also much more code than normal here, so I won't explain each and every expression
as I often do.

## Part One

Our input is a list of lines with `x,y` coordinates that are separated by a comma. I checked, and all values are
positive integers. Not only that, but there are exactly two lines that share a common `x` value, and two that share
a common `y` value, so that lets us make some assumptions later on. Our goal here is to figure out which two points
that, if they formed a rectangle, has the largest area. We return that area as our answer.

This part isn't bad at all! First, let's parse the input into a sequence of `[x y]` tuples for the points.

```clojure
(defn parse-points [input] (partitionv 2 (c/split-longs input)))
```

Next, we'll implement a helper function called `rectangle-area` to calculate the area of a rectangle based on two
points at opposite corners.

```clojure
(defn rectangle-area [[x0 y0] [x1 y1]]
  (* (inc (abs (- x0 x1)))
     (inc (abs (- y0 y1)))))
```

For both `x` and `y`, we take the absolute value of the difference in values since we don't know which one is
greater. Then before multiplying the values together, we increment both differences, as both points need to be
inclusive, and `(- x0 x1)` would leave off one of the edges. If the ordinates were adjacent, the mathematical
difference would be 1, but we want both points to count so it should be 2.

We already have what we need to implement `part1`.

```clojure
(defn part1 [input]
  (apply max (map (partial apply rectangle-area)
                  (c/unique-combinations (parse-points input)))))
```

We parse the input into points and call `c/unique-combinations` to find every unique pair of points. Then we
calculate the rectangle area for each such pair and find the max value. Easy! Just like part two, right? Right?

## Part Two

Never mind all the nonsense in the instructions about red and green tiles - we need to form a polygon by connecting
each of the points together and then finding the largest area from pairing two points such that their rectangle
completely fits within the polygon.

Here's the overall approach. First, we figure out the path we'll follow so we can conceptualize the shape. Then we'll
look at every possible row (y-value) contained within all points, and we'll come up with the range of x-values in the
row which are *not* contained by the polygon. Because the polygon will be large, many of the rows will have the same
gaps, so we'll merge the row-level data into "gap groups" - a range of rows and their identical ranges of gaps. Finally,
we'll look at the pairs of points that aren't on the same line, and return the largest one that doesn't have a gap on
any of its rows.

Let's start with `follow-path`, which takes in the collection of `points` and returns a sequence for the path we take to
get around the polygon. **UPDATE:** this function isn't needed, per the first refactoring below. But since I thought
we needed it, I'm keeping it here for a record.

```clojure
(defn follow-path [points]
  (let [first-point (first (sort points))]
    (loop [path [first-point], search (disj (set points) first-point), horizontal? true]
      (if (seq search)
        (let [dir-fn (if horizontal? first second)
              path-ord (dir-fn (last path))
              p' (c/first-when #(= path-ord (dir-fn %)) search)]
          (recur (conj path p') (disj search p') (not horizontal?)))
        (conj path (first path))))))
```

We sort the points so that our first point is farthest to the left and at the highest space in the grid. Then we loop
over the remaining points, each time between looking for the next value in the x-axis or y-axis. So the second point
will be horizontal from the first, then the next will be vertical, then horizontal, etc. As we find each point, we add
it to the end of the `path`, remove it from the `search` space, and flip the value of `horizontal?`. When we're all
done, we add the first point to the end of the path to make the polygon complete.

Next, we implement `row-gaps`, which takes in the min and max `x` values of the polygon's bounding box, the sorted
line `segments` in the path around the polygon (again, left to right, top to bottom) and the row number, and returns
a sequence of `[low high]` x-ordinate ranges that are *not* within the polygon. 

```clojure
(defn row-gaps [min-x max-x segments row]
  (->> (reduce (fn [[in-gap? found] [[x0] [x1]]]
                 [(not in-gap?) (if in-gap? (assoc-in found [(dec (count found)) 1] (dec x0))
                                            (conj found [(inc x1) max-x]))])
               [true [[min-x max-x]]]
               (filter (fn [[[_ y0] [_ y1]]] ((if (> y0 y1) >= <=) y0 row y1)) segments))
       second
       (remove (partial apply >=))))
```

We start by filtering the segments for the ones that intersect the `row`. Our `reduce` function is a vector of two
values - `in-gap?` which specifies whether we are currently in a gap as opposed to being in the polygon, and all
ranges of gaps we have found so far. Initially, we start in a gap that lasts until the first segment, and we assume
that the entire row, from `min-x` to `max-x` is one big gap. Then as we cross each segment, we replace both parts of
the accumulator. First, we flip `in-gap?` since each segment switches us from being "inside" or "outside" the polygon.
Then we update the gaps based on whether we're in a gap. If we are, then the gap is closed, so we change the `high`
value of the last gap to be the value before the current segment, as that segment is necessarily in the polygon. If we
were not in the gap, then we're discovering a new one, so we `conj` a new gap from the value after the segment's `x`
ordinate all the way to the end of the row. When `reduce` is done, we call `second` to discard the `in-gap?` working
boolean, and then remove all gaps where `low` isn't less than `high`. This can happen if, for instance, we find a gap
that starts at the end of the polygon; because a gap segment starts from `(inc x1)`, that can put it above `max-x`,
which is nonsensical.

Now it's time for `gap-groups`, and this takes a couple of seconds with the puzzle input. This function takes in the
complete list of points, and it returns an ordered sequence of `[row-range (gap-ranges)]`. Every row within
`row-range`, of the form `[y0 y1]`, has the same collection of gaps.

```clojure
(defn gap-groups [points]
  (let [path (follow-path points)
        segments (->> (partition 2 1 path)
                      (map (partial sort-by (juxt first second)))
                      (sort-by (juxt first second)))
        [[min-x min-y] [max-x max-y]] (p/bounding-box points)]
    (first (reduce (fn [[gaps last-gap :as acc] row]
                     (let [gap (seq (row-gaps min-x max-x segments row))]
                       (cond (nil? gap) (assoc acc 1 nil)
                             (= gap last-gap) (update-in acc [0 (dec (count gaps)) 0 1] inc)
                             :else (-> acc
                                       (update 0 conj [[row row] gap])
                                       (assoc 1 gap)))))
                   [[] nil]
                   (range min-y (inc max-y))))))
```

We start by calculating the path with `follow-path`, and then we sort the segments along the path as described. We also
reuse an old function `p/bounding-box` that we implemented in a previous Advent year. This function takes in a
collection of points and returns a tuple of `[[min-x min-y] [max-x max-y]]`, inclusive, that includes all points within.
Then we `reduce` over all `y` values in the bounding box, calculate the `row-gaps`, and then either add them to the
most recent gap group, form a new gap group because it's different from its predecessor, or discard it if the row has
no gaps at all.

We're almost there... sit tight. We implement `has-gap?` to test if a pair of points has any gap, meaning it overlaps
with any gap group.

```clojure
(defn has-gap? [gap-groups [[x0 y0] [x1 y1]]]
  (let [[y-min y-max] (apply (juxt min max) [y0 y1])
        gap-columns (->> gap-groups
                         (drop-while (fn [[[_ y]]] (< y y-min)))
                         (take-while (fn [[[_ y]]] (<= y y-max)))
                         (mapcat second))]
    (some (fn [[low high]] (or (<= x0 low x1) (<= low x0 high))) gap-columns)))
```

We start with the knowledge that the `gap-groups` are sorted by the `y-ordinate`, so we can be efficient. We drop all
gap groups whose `y` value is lower than the lower point of the rectangle, and then take gap groups until we go too far.
`(mapcat second gap-groups)` strips away row ranges so we just have one long sequence of gaps. Then the `some` function
returns true if any of the remaining gaps overlap with the rectangle's `x` values.

It's time to wrap it up!

```clojure
(defn part2 [input]
  (let [points (parse-points input)
        gap-groups (gap-groups points)]
    (->> (sort-by (juxt first second) points)
         (c/unique-combinations)
         (remove (fn [[[x0 y0] [x1 y1]]] (or (= x0 x1) (= y0 y1))))
         (remove (partial has-gap? gap-groups))
         (map (partial apply rectangle-area))
         (apply max))))
```

We parse the input and find the gap groups before doing our calculation. First we sort the points and find the
`c/unique-combinations` for every possible rectangle. We remove the ones where the two points share a common `x` or `y`
value, since those make lines, not rectangles, and there's no way they'll be the largest values. We then also remove
all rectangles that overlap with gaps. Finally, for the remaining rectangles that are contenders, we calculcate their
`rectangle-area` and select the largest value.

Whew! Quite a lot of code, and I'll bet there's a cleverer solution out there. But this is mine and I'm going to keep
it... at least until some late night this week when I keep coding instead of sleeping.

## Refactorings

### Don't compute the path

I didn't realize that the input data already lists the points in path order! So there's really no need for the
`follow-paths` function at all. All we need to update is the start of the `gap-groups` function to call
`(conj (vec points) (first points))` to make the list of points into a vector so we can attach the first point to 
the end with `conj`.

```clojure
(defn gap-groups [points]
  (let [path (conj (vec points) (first points))
    ...))
```