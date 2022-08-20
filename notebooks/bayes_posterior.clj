;; # Bayes' Posterior! 🌍
^{:nextjournal.clerk/visibility #{:hide-ns :hide}}
(ns bayes-posterior
  (:require [nextjournal.clerk :as clerk]
            [controls :as c]
            [graphs :as g]
            [dbinomial :as d]))

^{::clerk/visibility #{:hide} :nextjournal.clerk/viewer :hide-result}
(defonce random_samples (take 100 (repeatedly (fn [] (if (>= 0.6 (rand)) "W" "L")))))

;; We want to know how much of the globe is covered by water.

;; Let's use a slider to select how many random samples to take from points on the globe and we will count how many
;; times we find land at that point ("L") vs water ("W").
^{::clerk/viewer c/slider}
(defonce n (atom 0))

(def coll-p (map #(/ % 200) (range 0 201)))

(def samples (take @n random_samples))

(clerk/vl
  {
   :hconcat
   [(apply g/land-or-water (d/count-land-or-water samples))
    (g/probability-dis "Relative Likelihood" coll-p (d/r-likelihood-from-samples coll-p samples))
    (g/probability-dis "Posterior Probability (standardized)" coll-p
                       (->
                         (d/r-likelihood-from-samples coll-p samples)
                         d/standardize))]})

