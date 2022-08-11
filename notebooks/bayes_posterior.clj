;; # Bayes Posterior! 🌍
^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns ^:nextjournal.clerk/no-cache bayes-posterior
  (:require [nextjournal.clerk :as clerk]
            [controls :as c]
            [dbinomial :as d]))

(defonce precalc (repeatedly (fn [] (hash-map :Water-or-Land (if (>= 6 (rand 10)) "W" "L")))))

;; We want to know how much of the globe is covered by water.

;; Let's use a slider to select how many random samples to take.
^{::clerk/viewer c/slider}
(defonce n (atom 0))

(def samples (vec (take @n precalc)))

(clerk/vl {:data {:values samples}
           :mark     "bar",
           :encoding {:x {:field "Water-or-Land", :type "nominal", :axis {:labelAngle 0}},
                      :y {:field "b", :type "quantitative" :aggregate "count"}}})

(d/dbinom (count (filter (partial = (hash-map :Water-or-Land "W")) samples))
          (count samples)
          0.4)

