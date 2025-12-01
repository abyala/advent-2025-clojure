# Day 01: Secret Entrance

* [Problem statement](https://adventofcode.com/2025/day/1)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day01.clj)

## Intro

Back to Advent of Code again, and now I've been out of the industry for over two years. I'm excited to flex my coding
muscles again. And at least I know I can still do Day One puzzles!

## Part One

We're given an input file with a bunch of instructions on how to turn a circular numerical lock with values from 0 to
99 inclusive, starting at 50. Our goal is to count the number of times an instruction lands us on zero. An instruction
looks like `DXX` where `D` is a directional value of "`L`" or "`R`", and `XX` is a number.

For simplicity, we're going to make a single instruction called `rotate`, which takes in a position and an instruction,
and returns the new position.

```clojure
(defn rotate [pos instruction]
  (-> pos
      (({\R + \L -} (get instruction 0)) (parse-long (subs instruction 1)))
      (mod 100)))
```

We can this function does three things - takes the position, does some madness, and then calls `(mod x 100)` to use the
modulus to keep our values between 0 and 99. So what's the second line do? Well on the right side, we see
`(parse-long (subs instruction 1))`, which substrings the instruction after the `D` value, parsing out the remaining
number. What we want to do is to either add this value (for an "`R`") or subtract it (for an "`L`") from the current
position. For that, we'll apply the function `({\R + \L -} (get instruction 0))`, which pulls out the first character
of the string (like Java's `charAt` method), and then looks up the `+` or `-` operator function from a map. So we'll
either end up with `(+ pos XX)` or `(- pos XX)`. Easy enough.

With that worker out of the way, we can implement `part1`.

```clojure
(defn part1 [input]
  (->> (str/split-lines input)
       (reductions rotate 50 )
       (count-when zero?)))
```

Here we call `str/split-lines` to parse each line of the input string, and use `reductions` to call `reduce` on each
line, but returning the sequence of all values returned for all iterations, instead of just the last one. Finally, my
lovely util function `count-when` returns the number of values in that sequence that are zero.

## Part Two

The rules have changed now - we need to know how many total times the list of instructions causes the lock to touch
the value zero. I don't know why the math took me longer than expected, but I got there! Let's get started.

```clojure
(defn num-zeros [from to]
  (if (pos-int? to) (quot to 100)
                    (+ (quot (abs to) 100) (if (zero? from) 0 1))))
```

To start, the `num-zeros` function will tell us how many times we move onto the number 0 when moving from value `from`
to value `to`, where `to` is the result of adding the instruction's `XX` value **before** we apply the modulus. We can,
however, trust that `from` will always be non-negative, since by the time we're done with an instruction, we'll get
rid of all negative values.

The simplest way I found to do this was to treat `to` differently depending on whether or not it was a positive number.
If it is, then we call `(quot to 100)` to see how many hundreds the final value is; if we go from `99` to `207`, we
will hit zero twice. If `to` is negative, then we check how many negative hundreds the `to` value is, and add another
one if the starting value was positive; if we started at zero and went negative, it didn't "cross" zero.

We can also rewrite this to multiple ways, but I'm not sure if this is any clearer, honestly.

```clojure
(defn num-zeros [from to]
  (+ (if (and (pos-int? from) (not (pos-int? to))) 1 0)
     (quot (abs to) 100)))
```

Now, in preparation for the final code, let's refactor the `rotate` function to give us all of the information we need.
Namely, given a position and a number of clicks already seen, plus the new instruction, return the updated position and
number of clicks (a click is a time we crossed zero):

```clojure
(defn rotate [[pos clicks] instruction]
  (let [pos' (({\R + \L -} (get instruction 0)) pos (parse-long (subs instruction 1)))]
    [(mod pos' 100) (+ clicks (num-zeros pos pos'))]))
```

The `pos'` function looks like the old `rotate` function, but we're going to delay the call to `mod`. To provide the
new position, we call `(mod pos' 100)` as we had before. To return the number of clicks, we call
`(+ clicks (num-zeros pos pos'))`, increasing the old value by the number of zeros we just crossed.

If we ignore fixing `part1` from the refactoring, `part2` would look like this:

```clojure
(defn part2 [input]
  (last (reduce rotate [50 0] (str/split-lines input))))
```

Here we again split the lines, call `reduce` instead of `reductions`, since we only care about the final value, and
call `last` to get the second value (the number of clicks) from the result.

But of course, that's not how we roll here. Our goal is to always make a unified `solve` function that both `part1` and
`part2` call, no matter how ridiculous it looks. So, let's be silly!

```clojure
(defn solve [f input]
  (f (reductions rotate [50 0] (str/split-lines input))))

(defn part1 [input] (solve (partial count-when (comp zero? first)) input))
(defn part2 [input] (solve (comp last last) input))
```

The `solve` function takes in the transformation function that differentiates parts 1 and 2. But the meat of the
function goes back to calling `(reductions rotate [50 0] (str/split-lines input))` to return all of the tuples of
`[position clicks]` for us to manipulate, and then we call the `f` function around it.

`part1` passes in a function `(partial count-when (comp zero? first))` to take that sequence of tuples, check if the
first value (the position) is zero, and then counts them up. `part2` just calls `(comp last last)` to take the last
tuple for the final state, and then the count within it, to get the final click count.

Boom! Day one complete.