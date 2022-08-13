;; # Bayes' Posterior! 🌍
^{:nextjournal.clerk/visibility #{:hide-ns :hide}}
(ns ^:nextjournal.clerk/no-cache bayes-posterior-static
  (:require [nextjournal.clerk :as clerk]
            [dbinomial :as d]))

^{::clerk/visibility #{:hide} :nextjournal.clerk/viewer :hide-result}
(defonce random_samples (take 100 (repeatedly (fn [] (if (>= 0.6 (rand)) "W" "L")))))

;; We want to know how much of the globe is covered by water.

(d/binomial-dis-for [])

(d/binomial-dis-for (vec (take 1 random_samples)))

(d/binomial-dis-for (vec (take 2 random_samples)))

(d/binomial-dis-for (vec (take 3 random_samples)))

(d/binomial-dis-for (vec (take 4 random_samples)))

(d/binomial-dis-for (vec (take 5 random_samples)))

(d/binomial-dis-for (vec (take 10 random_samples)))

(d/binomial-dis-for (vec (take 15 random_samples)))

(d/binomial-dis-for (vec (take 20 random_samples)))

(d/binomial-dis-for (vec (take 40 random_samples)))

(d/binomial-dis-for (vec (take 50 random_samples)))

(d/binomial-dis-for (vec (take 100 random_samples)))
