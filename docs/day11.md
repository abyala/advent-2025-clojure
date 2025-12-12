# Day 11: Reactor

* [Problem statement](https://adventofcode.com/2025/day/11)
* [Solution code](https://github.com/abyala/advent-2025-clojure/blob/master/src/advent_2025_clojure/day11.clj)

## Intro

This was a cute little puzzle, especially after the horror that was day 10. It was nice to not run into any gotchas on
the last "full" day of Advent, and I feel like my solution was efficient. I did do a little rewriting of the first
part for the second, but there was so little work needed for part 1 that I'm not going to try and rewrite history
too much.

## Part One

Our input is a connections from a device to the devices it unidirectionally connects to, in the format
`"AAA: BBB CCC"`, and we need to count the number of paths that take us from the node `"you"` to `"out"`. Easy enough.
Let's start with parsing.

```clojure
(defn parse-input [input]
  (reduce #(let [[from & to] (re-seq #"\w+" %2)]
             (assoc %1 from (set to)))
          {}
          (str/split-lines input)))
```

When I was working on this, I had to decide whether this was the kind of puzzle that we'd calculate front-to-back,
meaning we'd need every outbound connection (`{"AAA" #{"BBB "CCC")}`), or if we'd need to look at it in reverse, 
showing all inbound connections (`{"BBB" #{"AAA"}, "CCC" #{"AAA"}}`). I wrote both just to be safe, but it turns out
we only need the more intuitive outbound connections.

The function is just a simple `reduce` over each line of the `input`, assembling the results into an empty map. For
each line, `(re-seq #"\w+" %w)` finds all words, meaning we can forget about the colon and the spaces. We destructure
this into `[from & to]`, so `from` binds to the first device, and `to` binds to the rest of the devices as a
collection.

Now let's compute `dist-to-out`, or the number of distinct paths from a device to the `out` device.

```clojure
(defn dist-to-out [outbounds]
  (letfn [(nested-dist [seen from]
            (if (seen from) seen
                            (let [seen' (reduce nested-dist seen (outbounds from))]
                              (assoc seen' from (c/sum seen' (outbounds from))))))]
    ((nested-dist {"out" 1} "you") "you")))
```

I hope you like nested functions, because we're going to use them a few times today! This function takes in the
`outbounds` map that we parsed in the beginning, and calls `nested-dist` with a goal of finding that max value from the
starting device `"you"`. We also pass along a `seen` caching map, initialized to show that the terminating device
`"out"` has 1 path to itself. Then we start making recursive calls. If `nested-dist` ever receives a `from` device it's
already seen, it just returns the cache. If it's the first time we see it, it makes a `reduce` call over its outbound
connections, recursively calling `nested-dist` and updating the `seen` cache with each pass. When it finishes updating
the cache with all outbound connections, it returns the revisecd cache after first adding together the sum of all
(now cached) distances from each of its children.

Now we can make a trivially simple `part1` function.

```clojure
(defn part1 [input] (dist-to-out (parse-input input)))
```

## Part Two

For part 2, we need to count the number of paths from a new starting device called `"srv"` to the same `"out"` device,
but we only want those paths that include both `"dac"` and `"fft"` along the path. The secret to this puzzle is that we
don't actually need to keep a record of every path from each device to the `out` node. Instead, we can cache just the
number of paths. More specifically, we cache a 4-element map with keys `:dac`, `:fft`, `:neither`, and `:both`.

To that end, let's start with the `merge-child-paths` function, which takes in two path-counting map plus the name
of the source device, and returns the map we get from combining them.

```clojure
(defn merge-child-paths [p0 p1 from]
  (let [add-over (fn [p [from to]] (-> p
                                       (update to + (from p))
                                       (assoc from 0)))
        p' (merge-with + p0 p1)]
    (case from "dac" (reduce add-over p' [[:neither :dac] [:fft :both]])
               "fft" (reduce add-over p' [[:neither :fft] [:dac :both]])
               p')))
```

Ignore the nested function `add-over` for a moment. We start with `(merge-with + p0 p1)` to combine the two path maps
`p0` and `p1` by adding their values together. Then we do a `case`, or a simple switch, on the value of the `from`
device. The idea is that if we hit `dac`, then all paths we thought hit neither device now include `dac`, so we want to
add the value of `:neither` into `:dac` before zeroing out `:neither`. Similarly, all paths that included only `:fft`
now include both, so we should add them together into `:both` and then zero out `:fft`. The `add-over` function does
that nicely, and using it allows us to make a single `reduce` expression for the two pairs of additions we need if the
device is either `"dac"` or `"fft". And of course, if the device is neither of them, just return the merged `p'` path
map.

So how do we use these maps? We rewrite `dist-to-out` as a new function `paths-to-out`.

```clojure
(def no-paths (zipmap [:neither :dac :fft :both] (repeat 0)))
(defn paths-to-out [outbounds from]
  (letfn [(nested-paths [dev seen]
            (if (seen dev) seen
                           (reduce (fn [acc to] (let [{p' to :as acc'} (nested-paths to acc)]
                                                  (update acc' dev merge-child-paths p' dev)))
                                   (assoc seen dev no-paths)
                                   (outbounds dev))))]
    ((nested-paths from {"out" (assoc no-paths :neither 1)}) from)))
```

First we define the constant `no-paths` as the binding of the four path map keys to zero. Then the function takes in the
map of outbound connections that we used before, along with the `from` device we need to calculate paths over to 
"out"`. As with `dist-to-out`, we define a nested function, now called `nested-paths`, which we initialize with the
`from` node and a mapping of `"out"` to a path map with a single path without either `"dac"` or `"fft`". When we're
done calculating the cache of maps, we'll pull out all information that originated from the `from` device.

Again, it should all look familiar now. If we've seen a device `dev`, return the cache path map. If this is our first
time, then return a `reduce` over the outbound connections from that device, working on an updated `seen` cache where
we associate a `no-paths` map for the device we're working on. For each connection, we recursively call `nested-paths`
with the current cache. We destructure this in a fun way, using the expression `{p' to :as acc'}`. This means we want
to create a binding called `p'` that is defined by the value of the map at the key defined by `to` (the outbound
device). The `:as acc` means that we also want to bind the entire map to `acc'`, so we can use both `p'` and `acc'` in
the resulting expression. All we do here is update this revised path map (`acc')` at the "from" device `dev` by
merging in the child paths we just found at `p'`.

We're ready to finish up!

```clojure
(defn part1 [input]
    (c/sum (vals (paths-to-out (parse-input input) "you"))))

(defn part2 [input]
  (:both (paths-to-out (parse-input input) "svr")))
```

`part1` parses the data and calculates the paths leading away from the `"you"` device. It doesn't care whether devices
it passes through, so we use `vals` to pull all values out of the map, and `c/sum` to add them together again. `part2`
calculates the paths from `"svr"`, and then pulls the value at the key `:both` since we only want that count for its
answer. Fun!
