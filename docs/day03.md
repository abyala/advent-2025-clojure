# Day 03: Lobby

* [Problem statement](https://adventofcode.com/2025/day/3)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day03.clj)

## Intro

After so many years of doing Advent puzzles, it's nice to sort of be able to anticipate how part 2 might look, or
what sorts of algorithms we need to apply to avoid performance issues. That was the case for part 2 today, which
made it much easier than it might have been in previous years! Let's get to it.

## Part One

We are given an input of multiple lines of numeric strings, 100-characters long in the puzzle data, which represent
battery banks. As a nice convenience, we learn that all characters are positive values, so no tricky zeroes to deal
with. Our goal is to select two digits in each bank to "switch on," combine them in-order to make the greatest possible
two-digit integer value, and then add them all up. Oh, and they define these two-digit numbers "joltages" just for fun.

So there's very little to do here. Most of the work will be in the appropriately-named `max-joltage` function.

```clojure
(defn max-joltage [bank]
  (->> (range 1 10)
       (keep #(when-some [idx (str/index-of bank (str %))]
                (parse-long (str % (last (sort (subs bank (inc idx))))))))
       (apply max)))
```

In this approach, which we may or may not completely rewrite in part 2, we're going to look for the first instance of
each possible numeric character, then the largest character to its right, smush them together, and find the largest
such value. We use `(range 1 10)` since there are no zeros, so that gives us the values `1` through `9`. In the `keep`
function, we use `str/index-of` to find the first instance of the string representation of that `1-9` digit, storing it
as `idx` if it's found; `keep` will discard any `nil` values arising from not finding that character. Then to find the
best ones digit, we use `(last (sort (subs bank (inc idx))))` - substring the bank to the right of the index, sort each
character value, sort them, and grab the largest one. Then concatenate that by calling `(str c1 c2)` and parse that
string right back into a number. I suppose we could have multiplied the first by ten and used some fancy arithmetic for
the ones, but that seemed silly. Finally, we find the `max` value to get us our answer.

```clojure
(defn part1 [input]
  (transduce (map max-joltage) + (str/split-lines input)))
```

Oh look - our first `transduce` of the season! And it's super simple - split the input into each line, transform each
with the `max-joltage` function, and add the accumulated values together.

## Part Two

Now we need to look for not the greatest 2 digits per bank, but the greatest 12. The naive solution we won't even
attempt is some recursive or tree-recursive option, or any kind of brute force algorithm. History shows it'll be too
slow.

Instead, our plan is to start from the end of the string, and slowly work our way to the front, looking for the greatest
possible values incrementally. So let's say we have a bank with value 4253. Starting with the last digit, all we can
make is a single selection, so a map of `{1 3}` tells us that the largest 1-digit result is `3`. Then we move to the `5`
and yield `{1 5, 2 53}`, because `1` is a better 1-digit value than `3`, and `53` is the only possible 2-digit.
Similarly, we'll then get `{1 5, 2 53, 3 253}` and finally `{1 5, 2 53, 3 453, 4 4253}`. So let's do it.

```clojure
(defn best-joltage [target-len bank]
  (get (reduce (fn [best c]
             (reduce (fn [acc len]
                       (update acc (inc len) (fnil max 0) (parse-long (str c (get acc len "")))))
                     best
                     (range (min (count best) (dec target-len)) -1 -1)))
           {}
           (reverse bank))
       target-len))
```

This function takes in both the `target-len` (12) and the `bank`. Oh, you know why that first argument is there...

I could have decomposed this function but didn't feel it was necessary. To read it, note that there are three parts:
a `get` and two nested `reduces`. The `get` says that once we're done building our map of best values, return the one we
want at the target length. The outer `reduce` is what drives the function from the last `bank` character to the first by
working off `(reverse bank)`. The meat of the algorithm is in the inner `reduce`.

That `reduce` works off `(range (min (count best) (dec target-len)) -1 -1)`, and there's a bunch to read there. First of
all, it's important to work backwards from the largest string to the smaller, since we're going to check if the next
character `c` should be thrown in front of any smaller length already discovered. If we add a character in front of a
2-digit number and store it as the best 3-digit number, we don't want to double-dip that again to build the best 4-digit
number. As a small efficiency, we don't care about any length greater than `target-len`, so we use the smaller starting
value of `(count best)` (the largest length already found from the outer `reduce`) and `(dec target-len)` so we can
possibly make a new best result of length `target-len`. Then for each possible length, we use `(get acc len "")` to find
the best value one digit smaller, or `""` if it's not found (only the first time), and append the character `c` in front
of it. We want to `update` the accumulated map by calling `max`, but if this is the first potential value of a certain
length, the `update` will find a `nil` previous value that will break the `max` function, so `(fnil max 0)` says "call 
the max function, but substitute a zero if the first argument is `nil`." And in this way, we build up our best map
quickly and linearly.

See? That wasn't so hard. So let's wrap it up.

```clojure
(defn solve [target-len input]
  (transduce (map (partial best-joltage target-len)) + (str/split-lines input)))

(defn part1 [input] (solve 2 input))
(defn part2 [input] (solve 12 input))
```

The `solve` function looks like the original `part1` function, except that instead of mapping with `max-joltage`, it
calls the partial function `best-joltage` with the correct `target-len`. And then `part1` calls `solve` with a
`target-len` of 2, and `part2` uses a `target-len` of 12. Hooray for Advent experience!
