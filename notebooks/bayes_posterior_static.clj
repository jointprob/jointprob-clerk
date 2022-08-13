;; # Bayes' Posterior! 🌍
^{:nextjournal.clerk/visibility #{:hide-ns :hide}}
(ns ^:nextjournal.clerk/no-cache bayes-posterior-static
  (:require [nextjournal.clerk :as clerk]
            [dbinomial :as d]))

^{::clerk/viewer :hide-result}
(defonce random_samples (take 100 (repeatedly (fn [] (if (>= 0.6 (rand)) "W" "L")))))

; We want to know how much of the globe is covered by land.

; We (simulate) spin(ning) the globe and take a sample of the random place where it lands.

; Before we have taken any samples, we have no knowledge of what portion of the world is land.
; All proportions of land vs water are equally likely.

(d/binomial-dis-for [])

; We spin the globe and get:

(take 1 random_samples)

; Now we have more information. We update our probabilities.

(d/binomial-dis-for (take 1 random_samples))

; Take another sample to get:

(take 2 random_samples)

(d/binomial-dis-for (take 2 random_samples))

; And another:
(take 3 random_samples)
(d/binomial-dis-for (take 3 random_samples))

; etc.....
(take 4 random_samples)
(d/binomial-dis-for (take 4 random_samples))

(take 5 random_samples)
(d/binomial-dis-for (take 5 random_samples))

(take 10 random_samples)
(d/binomial-dis-for (take 10 random_samples))

(take 15 random_samples)
(d/binomial-dis-for (take 15 random_samples))

(take 20 random_samples)
(d/binomial-dis-for (take 20 random_samples))

(take 40 random_samples)
(d/binomial-dis-for (take 40 random_samples))

(take 50 random_samples)
(d/binomial-dis-for (take 50 random_samples))

(take 100 random_samples)
(d/binomial-dis-for (take 100 random_samples))
