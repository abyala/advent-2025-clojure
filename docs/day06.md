# Day 06: Trash Compactor

* [Problem statement](https://adventofcode.com/2025/day/6)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day06.clj)

## Intro

Leave it to Advent to make a puzzle called Trash Compactor, where really that was just the intro but the puzzle itself
was Math Homework. Anyway, this was a fun puzzle because it focused on the parsing logic, which often is the "let's 
get this out of the way" part. I've opted to make my solution a little longer by putting in more helper functions this
time; you can get a lot done in a single line of Clojure, but sometimes it doesn't look as pretty as spacing things
out. Plus, you know, testability and reuse.

## Part One

We are given a math problem, expressed as a series of lines with oddly spaced numbers arranged in columns, with either
an `*` or `+` sign at the bottom to signify what operations to do to the numbers above. So if we can just parse the
input into columns of numbers, the rest should be simple. So let's do that.

First, let's write the `parse-columns` function, which will take in all input lines except for the last one, and returns
a sequence that contains each column-sequence of numbers.

```clojure
(defn parse-columns [lines]
  (apply map list (map c/split-longs lines)))
```

I'm going to start from the outside first. One of the fun facets of Clojure's `map` function is that it takes in a
mapping function and then any number of collections. Usually we pass in one, so `(map - [-1 0 3])` returns `(1 0 -3)`.
But if we pass in multiple collections, it applies the function to each consecutive value in each collection, so
`(map str ["a" "b" "c"] [1 2 3])` returns `("a1" "b2" "c3")`. Our goal is to call `(apply map list ...)` to combine
the `nth` value in multiple collections into lists, so `(apply map list [[1 2 3] [4 5 6]])` returns
`((1 4) (2 5) (3 6))`. We need to use `apply` here since we're not actually passing in multiple collections, but rather
a collection of collections, and `apply` breaks them apart. So what are we creating lists from? Calling
`(map c/split-longs lines)`, converting each line into a sequence of numbers. Remember that function from yesterday?

What did this actually do? We converted the lines of strings into lines of numbers, and then make a sequence of the
first number in each line, then the second number in each line, etc. Therefore, we hit the goal of returning a sequence
of each column-sequence of numbers.

Ok, now let's plan ahead for a moment. We're going to need to take in a sequence of numbers for each column and the
operator to run on them. Let's make a simple `calculate` function for that.

```clojure
(defn calculate [nums op]
  (apply ({"*" * "+" +} op) nums))
```

We could use `(if (= op "*") * +)` since we trust the input to only have those two string values, but I prefer to code
in a way that fails fast. So instead, we'll take the operator `op` and look it up in a map `{"*" * "+" +}` to return
the correct function for the operator; if somehow it's not there, the function will fail instead of silently adding.
So all we have to do is apply that operator to all numbers in the incoming list.

Now we can write `part1`.

```clojure
(defn part1 [input]
  (let [[num-str op-str] ((juxt butlast last) (str/split-lines input))]
    (apply + (map calculate (parse-columns num-str) (re-seq #"\S" op-str)))))
```

Let's start with everyone's favorite Clojure function, `juxt`. As a reminder, `juxt` takes in a number of functions, 
and it returns a function that applies each component function on its input, and puts the results into a vector of
results. In this case, after splitting the input string into separate lines, we need to call `butlast` and `last` to
effectively separate the initial lines from the last one. This returns the sequence of lines of numbers
`(butlast lines)`, and the last line `(last lines)`, which we immediately destructure into `num-str` and `op-str`.

Then it's a simple matter of doing the calculation. We again use `map` with multiple collection arguments - each column
of numbers combined with each operator, which we obtain by calling `(re-seq #"\S" op-str)`. `calculate` takes these two
arguments in that order, so we just `map` them, and then add the results together.

## Part Two

Well now we need to read the input lines differently. We still look at columns of strings together, but each number
within the column comes from reading vertically, not horizontally. It thought about trying to do with indexes,
treating the input lines as a grid of characters, but in this case I preferred continuing to work with strings. Let's
do some helper functions first.

```clojure
(defn parse-blankable-long [coll]
  (parse-long (str/trim (apply str coll))))

(defn pad-equal [lines]
  (let [pattern (str "%-" (apply max (map count lines)) "s")]
    (map #(format pattern %) lines)))
```

`parse-blankable-long` takes in a sequence of characters, some potentially being spaces, and parses the string. This
could include something like `(\space 1 2)` or `(3 4 \space)`, but we trust the logic wouldn't be `(5 \space 6)`
because that wouldn't make sense. Calling `(parse-long " 78 ")` returns `nil`, so we need to get rid of the whitespace.
So we just call `(apply str coll)` to combine each character into a string, then `str/trim` to trim it, and finally
`parse-long` to return the number.

`pad-equal` takes in a sequence of lines and right-pads them with spaces to make them equal in length; we can use the
string `format` command to that end. First we create the format pattern, which takes the shape `"%-Xs"` where `X` is the
target length. We determine that length with `(apply max (map count lines))` by finding the largest line in the
sequence. Then we simply `map` `(format pattern %)` onto each line.

Alright, now let's implement `parse-cephalopod-columns` (did I mention that it's cephalopods who need help with their
math homework?) based on these helper functions.

```clojure
(defn parse-cephalopod-columns [lines]
  (let [padded (pad-equal lines)]
    (->> (apply interleave padded)
         (partition-all (count padded))
         (map parse-blankable-long)
         (partition-by nil?)
         (remove #(= % [nil])))))
```

Remember that we're taking in all input lines except for the last one with the operators, so we call `pad-equal` to get
them all into equal-length strings. We call `(apply interleave padded)` to create a sequence of characters, one at a
time from each line. Then `(partition-all (count padded))` formats this single sequence into a sequence of sequences,
representing each single-length column of numbers and spaces. `(map parse-blankable-long)` converts those characters
into a number or `nil` if it's all spaces, as will appear between columns of numbers. `(partition-by nil?)` groups
values together, such that we get an interleaving of sequences of numbers and sequences of a single `nil`. This provides
the groupings of numbers into the columns we'll be calculating together. Finally, `(remove #(= % [nil]))` removes those
blank values since we don't need them anymore.

So it's a little hard to read, but it's straightforward once you know what each step is doing.

Now we're ready to finish, and the code is so simple to the original `part1` that we can go straight to the shared
`solve` function.

```clojure
(defn solve [parse-fn input]
  (let [[num-str op-str] ((juxt butlast last) (str/split-lines input))]
    (apply + (map calculate (parse-fn num-str) (re-seq #"\S" op-str)))))

(defn part1 [input] (solve parse-columns input))
(defn part2 [input] (solve parse-cephalopod-columns input))
```

`solve` takes in both a parsing function and the input; `part1` uses the old `parse-columns` and `part2` uses the new
`parse-cephalopod-columns`. Then `solve` just repeats what `part1` does, but instead of calling
`(parse-columns num-str)`, it calls `(parse-fn num-str)` since that's what varies from part 1 to part 2.

Cute puzzle! I'll bet there were much simpler ways to do the parsing logic, so I'm curious to read other solutions.
