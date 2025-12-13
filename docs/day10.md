# Day 10: Factory

* [Problem statement](https://adventofcode.com/2025/day/10)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day10.clj)

## Intro

Well this was miserable. I spent hours trying to do part 2 of the puzzle, only to learn that this can only be solved
with linear algebra (which I don't know), and/or a third party library. So let's keep the fun part and ignore what I'm
going to write for part 2.

## Part One

We're given a series of lines that represent "machines", including a target diagram we want to reach, a series of
buttons we can push to flip our lights on or off, and a section of "joltages" we'll never use. We need to find the
fewest number times we can push any button to end up with lights that match the target diagram, and then sum those into
a grand total of button pushes.

As usual, let's start with parsing. Each machine should be a map with keys `:diagram`, `:schematics`, and `:joltages`.

```clojure
(defn parse-machine [s]
  (let [[_ diagram schematic-str _] (re-find #"\[([^\]]+)\] ([^{]+) (.*)" s)]
    {:diagram    (mapv (partial = \#) diagram),
     :schematics (set (map c/split-longs (str/split schematic-str #" ")))
     :joltages   (vec (c/split-longs (last (re-find #"\[([^\]]+)\] ([^{]+) (.*)" s))))}))
```

Yeah, regular expressions! We create three capture groups. First, we grab everything between the square brackets that
surround the diagram; we could have just taken everything before the first space and trimmed off the first and last
characters, but I liked this better since we're already in a regex. Second, we take the entire string up to the space
before the curly brace. And then finally, we take everything in the end. For the `diagram`, we create a vector of
booleans, representing whether the target value at that index should be on (`true`) or off (`false`). For the 
`schematics`, we split the one big string by spaces to make a list of strings with numbers, and then map them to
`c/split-longs` so each schematic has only numbers in it. Then we turn the whole thing into a `set` for ease of use
later. And as for `joltages`, well, read it if you want but we don't use it.

Now let's make a simple function called `push-buttons`, which takes in a vector of lights (booleans) and a vector for 
the schematic (numbers), and returns the new state of the lights after pushing the buttons within the schematic.

```clojure
(defn push-buttons [lights schematic]
  (reduce #(update %1 %2 not) lights schematic))
```

Really simple here! We `reduce` over the `schematic` vector, starting with the initial state of the `lights`. For each
button in the schematic (the index of the light to switch), we call `(update %1 %2 not)` to flip the boolean value of
the light using `not`.

Now let's do the bulk of the logic, `wire-up`, which returns the minimum number of button presses needed to make the
lights match the pattern in a machine.

```clojure
(defn wire-up [{:keys [diagram schematics]}]
  (loop [queue (conj PersistentQueue/EMPTY [(vec (repeat (count diagram) false)) schematics])
         seen #{}]
    (let [[lights unused :as option] (peek queue)]
      (cond (= lights diagram) (- (count schematics) (count unused))
            (seen option) (recur (pop queue) seen)
            :else (recur (reduce conj (pop queue) (map #(vector (push-buttons lights %) (disj unused %)) unused))
                         (conj seen option))))))
```

We're going to do a depth-first search, and in the past I would implement this myself. However, I found in a previous
puzzle this year, someone used one of Clojure's built-in `PresistentQueue`s, and I thought I'd give it a shot too. It
does just what you would expect - it supports `conj`, `peek`, and `pop`, which is pretty much what we need. To
initialize our loop, we immediately `conj` our search option, which takes the form of `[lights schematics]` for the
"current" state of the lights and the schematics we haven't pushed yet, plus a cache of the states we've seen. One of
the important concepts for this puzzle is that there's never a reason to push a button twice, since the second push
undoes the first. So our queue of options to try includes the schematics we _haven't_ used yet.

Then we iterate. We grab the front of the queue and check to see if the enabled lights match the target diagram. If so,
then we return the number of schematics used, which is the total count minus the count we didn't use. If the option
already exists in the queue, skip over it. Otherwise, we try pushing the buttons for each remaining schematic onto the
current lights, remove that schematic from the collection of unused ones, and throw them back in the queue, along with
adding the current option into the `seen` cache.

```clojure
(defn part1 [input]
  (c/sum wire-up (map parse-machine (str/split-lines input))))
```

Finally, we finish our solution. All we need to do is parse the input into the collection of machines, and then sum
them up by calling `wire-up` on each of them.

## Part Two

Part two was some bullshit, and I'm just not interested in researching both complex math or third party libraries for
what should be a fun exercise in writing code. So I'll confess - I found someone else's solution online and just ran
the damned thing with my input. It's lame, and I won't even insult the other developer by giving them credit (but good
job for spending your precious time on this). But yeah, I cheated my way to this star, and I'm ok with that.