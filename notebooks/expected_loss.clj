^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns ^:nextjournal.clerk/no-cache expected-loss
  (:require [nextjournal.clerk :as clerk]
            [graphs :as g]
            [fastmath.protocols]
            [fastmath.stats :as stats]
            [fastmath.random :as r]
            [dbinomial :as d]))


; # Calculating expected loss / error in Clojure using [fastmath](https://github.com/generateme/fastmath) library

; Let's look at different ways by which we will calculate the optimal choice for d to minimize our expected error
; and what we expect that error to be.

; We want to minimize our linear error abs(d- p).

;; ## Preparation

;; We will use [fastmath](https://github.com/generateme/fastmath) library. 

(require '[fastmath.random :as r]
         '[fastmath.core :as m]
         '[fastmath.stats :as stats]
         '[fastmath.kernel :as k])

;; Let's slice range into 201 values (grid)

(def p-grid (m/slice-range 0.0 1.0 201))

;; The step between grid bins (need to decrement count as this is an inclusive scale between 0 and 1).
(def grid-step (/ 1 (dec (count p-grid))))

;; ### First let's create a posterior distribution having seen 6 water samples in 9 random samples.

(def prob-data (map #(r/pdf (r/distribution :binomial {:trials 9 :p %}) 6) p-grid))

(def posterior (let [sum (stats/sum prob-data)]
                 (map #(/ % sum) prob-data)))


;; ### Drawing samples

;; And create samples of this distribution.

(def posterior-distr (r/distribution :real-discrete-distribution {:data p-grid :probabilities posterior}))

(def samples (r/->seq posterior-distr 10000))

(def samples-density (frequencies (map #(* (m/floor(/ % grid-step)) grid-step) samples)))

(clerk/vl
 {:hconcat [(g/point-chart "Posterior Samples" "Sample number" "proportion water (p)" (range) samples "blue" 1)
            (g/point-chart "Posterior Sample Density" "proportion water (p)" "Posterior Sample Density" (keys samples-density)
                          (d/standardize(vals samples-density)) "blue" 5)]})

;; ## Different ways of choosing d and calculating expected error

;; Let's look at various ways of choosing d and calculating the expected error.

;; ### Median

;; Theory says the median of the samples should minimize the expected error, the median of the samples is 
;; the value of p that splits the probability density in half.
(def median-of-samples (stats/median samples))

;; ### Minimize Average Error over all samples

;; We can also define a function to calculate the average error (absolute difference between d and p)
;; for all samples.
(defn error-from-samples [samples d]
  (/ (reduce + (map (fn [s] (m/abs (- s d))) samples))
     (count samples)))


;; We're going to find the minimum of this curve:

(clerk/vl
 (g/line-chart "loss" "decision" "expected error from samples" p-grid (map (partial error-from-samples samples) p-grid)))

(def d-from-samples (apply min-key (partial error-from-samples samples) p-grid))

;; This gives expected error:

(error-from-samples samples d-from-samples)

;; Now we have the function to calculate the average error for the distribution of p we expect we can also calculate the 
;; expected error from the median.

(error-from-samples samples median-of-samples)

;; ### Calculate directly from posterior.

;; Calculating d from the posterior will be more accurate (avoiding sampling error). We calculate the average of the
;; product of the likelihood of the value of p and abs(d - p).

(defn error-from-posterior [p-grid posterior d]
  (reduce + (map (fn [p likelihood] (* likelihood (m/abs (- d p)))) p-grid posterior)))

;; We're now going to find the minimum of this curve:

(clerk/vl
 (g/line-chart "loss" "decision" "expected error from posterior" p-grid (map (partial error-from-posterior p-grid posterior) p-grid)))


(def d-from-posterior (apply min-key (partial error-from-posterior p-grid posterior) p-grid))

(error-from-posterior p-grid posterior d-from-posterior)

(error-from-samples samples d-from-posterior)

;; ## Let's calculate the expected error vs the actual error 1,000 times and plot them.

; For 10, 100 and 1,000 evidence samples and for random values of p (the proportion of water on the globe) which is the unobserved variable.

(defn expected-error-from-random-samples-vs-real-loss [n p]
  (let [data (repeatedly n #(r/randval p :W :L))
        water-count (count (filter #{:W} data))
        prob-data (map #(r/pdf (r/distribution :binomial {:trials n :p %}) water-count) p-grid)
        sum (stats/sum prob-data)
        posterior (map #(/ % sum) prob-data)
        d (apply min-key (partial error-from-posterior p-grid posterior) p-grid)
        expected-loss (error-from-posterior p-grid posterior d)
        ] 
     {:water-count water-count
      :expected expected-loss
      :actual (m/abs (- p d))
      :expected-minus-actual (- expected-loss (m/abs (- p d)))}))


(def expected-vs-actuals 
  (map (fn [n] (list n (repeatedly 1000 #(expected-error-from-random-samples-vs-real-loss n 0.6)))) 
       [10 100 1000]))

;; ## Finally let's plot the expected and actual error and the (expected - actual error for each trial)

;; The blue lines on these graphs are the means for 1,000 trials.

;; The unobserved parameter of this model that we are trying to infer is p = 0.6.
;; The evidence is our n :W or :L samples.

(clerk/vl {:hconcat (map
                     (fn [[n e-vs-a]]
                       (hash-map :vconcat
                        [{:layer [(g/point-chart (str "Expected Error (n=" n ")")
                                                 "Water count"
                                                 "Expected"
                                                 (map :water-count e-vs-a)
                                                 (map :expected e-vs-a)
                                                 "red")
                                  (g/line-chart (str "Mean")
                                                "Water count"
                                                "Mean"
                                                [0 (apply max (map :water-count e-vs-a))]
                                                (repeat (stats/mean (map :expected e-vs-a))))]}
                         {:layer [(g/point-chart (str "Actual Error (n=" n ")")
                                                 "Water count"
                                                 "Actual"
                                                 (map :water-count e-vs-a)
                                                 (map :actual e-vs-a)
                                                 "black")
                                  (g/line-chart "Mean"
                                                "Water count"
                                                "Mean"
                                                [0 (apply max (map :water-count e-vs-a))]
                                                (repeat (stats/mean (map :actual e-vs-a))))]}
                         {:layer
                          [(g/point-chart (str "Expected - Actual Error (n=" n ")")
                                          "Water count"
                                          "Expected - Actual Error"
                                          (map :water-count e-vs-a)
                                          (map :expected-minus-actual e-vs-a)
                                          "purple")
                           (g/line-chart "Mean"
                                         "Water count"
                                         "Mean"
                                         [0 (apply max (map :water-count e-vs-a))]
                                         (repeat (stats/mean (map :expected-minus-actual e-vs-a))))]}]))
                       expected-vs-actuals)})


;; ### Mean expected and actual loss over 1,000 trials and mean of the difference

;; Here are the mean values of :expected and :actual error for different amounts of evidence (:n).

;; And would you break even on average if you bet that the actual error was less than expected error?
;; What is the average of each (:expected - :actual) across the 1,000 trials?

(clerk/table (map (fn [[n e-vs-a]] {:n n
                                    :mean-expected-error (stats/mean (map :expected e-vs-a))
                                    :mean-actual-error (stats/mean (map :actual e-vs-a))
                                    :mean-expected-minus-actual (stats/mean (map :expected-minus-actual e-vs-a))}
                    ) expected-vs-actuals))




