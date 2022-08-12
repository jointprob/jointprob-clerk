;; # Bayes' Posterior! 🌍
^{:nextjournal.clerk/visibility #{:hide-ns :hide}}
(ns bayes-posterior
  (:require [nextjournal.clerk :as clerk]
            [controls :as c]
            [dbinomial :as d]))

^{::clerk/visibility #{:hide} :nextjournal.clerk/viewer :hide-result}
(defonce random_samples (take 100 (repeatedly (fn [] (hash-map :Water-or-Land (if (>= 0.6 (rand)) "W" "L"))))))

;; We want to know how much of the globe is covered by water.

;; Let's use a slider to select how many random samples to take from points on the globe and we will count how many
;; times we find land at that point ("L") vs water ("W").
^{::clerk/viewer c/slider}
(defonce n (atom 0))

(d/binomial-dis-for (take @n random_samples))


