;; # Bayes' Posterior! 🌍
^{:nextjournal.clerk/visibility #{:hide-ns :hide}}
(ns ^:nextjournal.clerk/no-cache bayes-posterior
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

^{::clerk/visibility #{:hide} :nextjournal.clerk/viewer :hide-result}
(def samples (vec (take @n random_samples)))

^{::clerk/visibility #{:hide} :nextjournal.clerk/viewer :hide-result}
(def found_land (count (filter (partial = (hash-map :Water-or-Land "W")) samples)))

^{::clerk/visibility #{:hide} :nextjournal.clerk/viewer :hide-result}
(def binomial-distribution
  (map
    (fn [r] (let [p (/ r 500)]
              (hash-map :x p :y (d/dbinom found_land @n p))))
    (range 0 501)))

^{::clerk/visibility :hide}
(clerk/vl
  {:hconcat
   [
    {:title    (str "N = " @n)
     :data     {:values samples}
     :mark     "bar",
     :encoding {:x {:field "Water-or-Land", :type "nominal", :axis {:labelAngle 0}},
                :y {:type "quantitative" :aggregate "count"}}}

    {:title    (str "Probability Distribution (N = " @n ")")
     :data     {:values binomial-distribution}
     :mark     "line",
     :encoding {:x {:field "x", :type "quantitative", :axis {:labelAngle 0}},
                :y {:field "y" :type "quantitative"}}}]})



