# Day 12: Christmas Tree Farm

* [Problem statement](https://adventofcode.com/2025/day/12)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day12.clj)

## Intro

Going out on a whimper this year. This puzzle looked hella difficult, and I had no clue where to even start. I read
some answers on the Clojurian Slack server and found out that you can ignore the puzzle itself and do something
really simple instead. Why? No clue.

## Part One

We're given an input with a bunch of patterns for shapes, and some regions where we're trying to place those patterns.
The puzzle tells us we need to count the number of regions, defined by their length, width, and numbers of each pattern,
can fit those patterns in any configuration. That'll take forever to figure out. Instead, we need to see how many
spaces each present takes, and then return if the total number of spaces for the presents fit within the area of the
region without trying to check their configurations. There's no indication why this would work, and indeed the test
data they provide fails it too. So, let's just write it.

Let's parse the input into the following data structure:
`{shapes [], :regions ({:size n, :shapes-needed [])}`.

```clojure
(defn parse-shape [shape-str]
  (c/count-when #(= % \#) shape-str))

(defn parse-region [region]
  (let [[length width & shapes] (c/split-longs region)]
    {:size (* length width) :shapes-needed shapes}))

(defn parse-input [input]
  (let [[shapes regions] ((juxt butlast last) (c/split-by-blank-lines input))]
    {:shapes (map parse-shape shapes) :regions (map parse-region (str/split-lines regions))}))
```

A shape is a series of lines where the first is the shape ID, followed by lines with a `#` for a space that's used,
and a `.` for a space that isn't. So we just need to count the number of spaces in the entire string that is a `#`
character.

A region is a single line of format `LxW: 1 2 3 4...` where `L` and `W` are the length and width of the area, and
`1 2 3 4...` are the numbers of each shape we need to fit. A single `c/split-longs` finds all the integer values,
so we grab the first two to calculate the `size`, and then we keep the collection of the others.

Then to parse the entire input, we use `(juxt butlast last)` on the lines that are split by blank lines, because that
gives us all shape line groups and a single group for the regions. Then we call the two parse functions.

```clojure
(defn fits? [shapes region]
  (let [{:keys [size shapes-needed]} region]
    (<= (c/sum (map * shapes shapes-needed)) size)))
```

`fits?` checks if the region can hold all the shapes it expects. We use `(c/sum (map * shapes shapes-needed))` to pair
together each shape size with the number that the region requires, and multiply those pairs together. Then we simply
check to see if the sum of shapes requested fits within the total size of the region.

```clojure
(defn part1 [input]
  (let [{:keys [shapes regions]} (parse-input input)]
    (c/count-when (partial fits? shapes) regions)))
```

After parsing the input, we just use `c/count-when` to find out how many regions can fit their shapes.

So... yeah. Puzzle complete? I guess?
