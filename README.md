# osc-multitoggle

Easily keep track of TouchOSC multitoggle state, meant for use in Overtone.

# Why?

Working with TouchOSC widgets is fairly straightforward. However "doing stuff" with multitoggle is not, since the client is only sending you information about the updated grid element, not the entire grid. osc-multitoggle manages state and creates the handlers for you.

# Usage

osc-multitoggle *only* manages state and handlers. It does not create a drum machine for you out of the box, but see an example below to accomplish that.

`(add-multitoggle-handler server layout widget rows columns)` creates your handlers for you.
The OSC client will send you something like `/2/multitoggle/3/4`. In my case, my multitoggle has 6 rows and 16 columns, so to create my handlers I would do: `(add-multitoggle-handler server 2 "multitoggle" 6 16)`.

Multitoggle state is kept in an atom called `grid`. State is stored in nested maps as follows: `layout -> widget -> column -> row`. So if I have recieved on "on" value from the client for row 1, column 2 in a multitoggle called "multitoggletest" in layout 1, the atom contents would be: `{"1" {"multitoggletest" {"2" {"1" 1.0}}}}`.

The structure was designed so that you could keep track of multitoggle state across multiple multitoggles in multiple different layouts.

## Example

### A Drum Machine In About 20 Lines Of Code

```clojure
(ns neatodrums.core
  (:use overtone.live
        overtone.inst.drum
        osc-multitoggle.core))

(def server (osc-server 44100 "osc-clj"))

(zero-conf-on)
(osc-listen server (fn [msg] (println msg)) :debug)
(def rows 6)
(def columns 16)

(add-multitoggle-handler server 2 "multitoggle" rows columns)

; Our grid has 16 columns. Subdivide the beat and treat each column as an eighth note.
(def one-twenty-bpm (metronome 240))

(defn play-column [layout widget col]
  (let [activedrums (get-active-in-row layout widget col)]
    (doseq [drum activedrums]
      (case drum
        "1" (kick)
        "2" (snare)
        "3" (kick2)
        "4" (kick3)
        "5" (open-hat)
        "6" (closed-hat)))))

(defn looper [nome]
  (let [beat (nome)
        beat-at (mod beat columns)]
    (at (nome beat) (play-column "2" "multitoggle" (str (+ beat-at 1))))
    (apply-at (nome beat) looper nome [])))

(looper one-twenty-bpm)
```

# TODO

* Better documentation
* More testing
* Unit tests

## License

Copyright © 2014 Sean Brewer

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

