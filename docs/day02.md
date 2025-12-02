# Day 02: Gift Shop

* [Problem statement](https://adventofcode.com/2025/day/2)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day02.clj)

## Part One

We are given a single line with a bunch of inclusive numeric ranges in the form `XX-YY`, separated by commas, all as a
single line. We need to look at all numbers within these ranges, and sum the values of those where the number, when
seen as a string, is a duplicated substring (aka "123123"), or what the puzzle calls an invalid ID.

Let's start by parsing the input into a sequence of two-element values for each low-high pair:

```clojure
(defn parse-input [input]
  (partition 2 (map parse-long (re-seq #"\d+" input))))
```

In the past I would have used a more complex regular expression to find all `XX-YY` strings that were separated by a
comma, but I've learned to simplify. We simply look for all strings of numeric digits, parse them into longs, and then
call `(partition 2 collection-of-longs)` to pair them up. I would also usually turn these lists into vectors, but it's
not needed.

Now let's make a function called `invalid-id?` to test if a number `n` is made of a doubled/duplicated substring.

```clojure
(defn invalid-id? [n]
  (let [s (str n)
        pivot (/ (count s) 2)]
    (= (subs s 0 pivot) (subs s pivot))))
```

This is nothing complex. We hold on to the number as a string using `(str n)`, and then figure out the halfway point
based on its length `(count s)`. Then we return `true` if the first half of the string matches the second half.

Now that we've figured that out for a single number, let's implement `invalid-ids` to return all invalid-id values
within a range.

```clojure
(defn invalid-ids [[low high]]
  (filter invalid-id? (range low (inc high))))
```

First off, note that the function argument is our 2-element list, which we immediate deconstruct in the function
definition into the low and high values. Then we call `filter` with our handle `invalid-id?` function on all values
within the *inclusive* range, meaning `(range low (inc high))` since the `range` function excludes the value of the
second argument.

Ok, we're ready to solve the first part.

```clojure
(defn part1 [input]
  (apply + (mapcat invalid-ids (parse-input input))))
```

We parse the input, and for each range we call `invalid-ids`. Since that would return a sequence of numeric sequences,
we call `mapcat` to flat map them into a single sequence of numbers, and then add them together with `(apply +)`.
Onward!

## Part Two

Now when we look for invalid-ids, we need to return those where there is any substring that repeats at least once, so
`123123123` would also count as an invalid ID. So let's do a little refactoring to the `invalid?` function to support
both models.

```clojure
(defn invalid? [only-doubles? n]
  (when (> n 9)
    (let [s (str n)
          len (count s)
          pivot (/ len 2)
          lengths (if only-doubles? [pivot] (range 1 (inc pivot)))]
      (some #(= s (repeat-string (/ len %) (subs s 0 %))) lengths))))
```

There's more going on, mostly because I added some helper bindings for clarity. First of all, we're going to recognize
one of my earlier bugs on single-digit numbers, as they can never have a duplicated value, so we'll only check the
input argument if it's greater than `9`. Then we'll once again turn it into a string, figure out its length and pivot
value, and then come up with the possible `lengths` to check for duplicates. If we in part 1, the only length of
substring we wish to inspect is the pivot length at the midpoint, so return `[pivot]`. Otherwise, return all values from
`1` to `pivot`, inclusive.

With those possible substring lengths to inspect, we want the function to return `true` if there is any such length that
duplicates into the whole string. Originally I checked for lengths that were divisible by the total length of the total
string, but it didn't prove to be necessary. So instead, we're going to leverage the `repeat-string` from my 
[Advent core utility library](https://github.com/abyala/advent-utils-clojure), since it creates a string from concatenating a substring `n` number of times. So
here, we want to see if the total string `s` is the same as taking the substring `(subs s 0 %)` and repeating it
`(/ len %)` times, meaning the number of times that length divides into the total length of the string.

Now we're ready to finish the puzzle.

```clojure
(defn invalid-ids [only-doubles? [low high]]
  (filter (partial invalid? only-doubles?) (range low (inc high))))

(defn solve [only-doubles? input]
  (apply + (mapcat (partial invalid-ids only-doubles?) (parse-input input))))

(defn part1 [input] (solve true input))
(defn part2 [input] (solve false input))
```

First, we update `invalid-ids` to take in the new `only-doubles?` argument to be passed into `invalid?`. Then we make
our `solve` function look similar to the original `part1` function, where we parse the input, call `invalid-ids` with
the passed-in `only-doubles?` argument, `mapcat` the results, and add them up. Finally, the new `part1` calls `solve`
with `true` since we only want doubles, while `part2` calls it with `false` since we're ok with any substring length.