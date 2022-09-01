^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns ^:nextjournal.clerk/no-cache ch3-code-snippets
  (:require [nextjournal.clerk :as clerk]
            [graphs :as g]
            [nextjournal.clerk-slideshow :as slideshow]
            [fastmath.random :as r]
            [fastmath.protocols]))

^{::clerk/viewer :hide-result
  ::clerk/visibility :hide}
(clerk/add-viewers! [slideshow/viewer])

;; # Chapter 3 code snippets

;; ### Reimplementation of all examples in Clojure using [fastmath](https://github.com/generateme/fastmath) library

;; *This is a slide show - use the left and right arrow keys to move back and forth and the escape key to see all slides.*

;; ----

;; # Preparation

;; I use [fastmath](https://github.com/generateme/fastmath) library for my implementation. 

(require '[fastmath.random :as r]
         '[fastmath.core :as m]
         '[fastmath.stats :as stats]
         '[fastmath.kernel :as k])

;; ----

;; ### 3.1 Sampling from grid-approximate posterior

;; Let's slice range into 1000 values (grid)

(def p-grid (m/slice-range 0.0 1.0 1000))

(def prob-data (map #(r/pdf (r/distribution :binomial {:trials 9 :p %}) 6) p-grid))

;; Prior pdf is always `1.0` (Uniform prior), we can skip defining `prob-p` variable and create posterior by normalizing `prob-data`

(def posterior (let [sum (stats/sum prob-data)]
               (map #(/ % sum) prob-data)))

;; ----

;; ### Drawing samples

;; First let's create discrete distribution with known probabilities (posterior)

(def posterior-distr (r/distribution :real-discrete-distribution {:data p-grid :probabilities posterior}))
(def samples (r/->seq posterior-distr 10000))

(def samples-density (k/kernel-density :epanechnikov samples))

(clerk/vl
 {:hconcat [(g/point-chart "Samples" "Sample number" "proportion water (p)" (range) samples)
            (g/line-chart "Density" "proportion water (p)" "Density" p-grid
                          (map samples-density p-grid))]})


;; ----

;; ### 3.2 Sampling to summarize

;; Add up posterior probability where p < 0.5

(->> (map vector p-grid posterior)
     (filter #(< (first %) 0.5))
     (map second)
     (stats/sum))

;; We can also use CDF from distribution

(r/cdf posterior-distr 0.5)

;; or samples

(/ (count (filter #(< % 0.5) samples)) 10000.0) 

;; ----

;; ### Figures 3.2

(clerk/example
 (/ (count (filter #(< % 0.5) samples)) 10000.0)
 (/ (count (filter #(< 0.5 % 0.75) samples)) 10000.0)
 (stats/quantile samples 0.8)
 (stats/quantiles samples [0.1 0.9]))

(clerk/vl
 {:hconcat [(g/distribution-with-area "Interval" "proportion water (p)" "Density" p-grid posterior {:lt 0.5})
            (g/distribution-with-area "Interval" "proportion water (p)" "Density" p-grid posterior {:range [0.5 0.75]})
            (g/distribution-with-area "Interval" "proportion water (p)" "Density" p-grid posterior {:lt (stats/quantile samples 0.8)})
            (g/distribution-with-area "Interval" "proportion water (p)" "Density" p-grid posterior
                                      {:range (stats/quantiles samples [0.1 0.9])})]})

;; ----

;; ### 3.2.2 Intervals of defined mass

(def likelihood (map #(r/pdf (r/distribution :binomial {:trials 3 :p %}) 3) p-grid))

(def posterior2 (let [sum (stats/sum likelihood)]
                (map #(/ % sum) likelihood)))

(def posterior-distr2 (r/distribution :real-discrete-distribution {:data p-grid :probabilities posterior2}))
(def samples2 (r/->seq posterior-distr2 10000))

;; ----

;; ### Figures 3.3

;; `fastmath` extents return a triplet: `[lower bound, upper bound, some statistic]`. Here this statistic is the median.

(clerk/example
 (stats/pi-extent samples2 0.5)
 (stats/hpdi-extent samples2 0.5))

(clerk/vl
 {:hconcat [(g/distribution-with-area "50% Percentile Interval" "proportion water (p)" "Density"
                                      p-grid posterior2 {:range (butlast (stats/pi-extent samples2 0.5))})
            (g/distribution-with-area "50% HPDI" "proportion water (p)" "Density" p-grid posterior2
                                      {:range (butlast (stats/hpdi-extent samples2 0.5))})]})

;; ----

;; ### 3.2.3 Point estimates

(defn chainmode
  [vs]
  (let [dd (k/kernel-density :epanechnikov vs 0.01)]
    (apply max-key dd vs)))

(clerk/example
 (apply max-key (partial r/pdf posterior-distr2) p-grid)
 (chainmode samples2)
 (stats/mean samples2)
 (stats/median samples2))

;; Code 3.17
(reduce + (map (fn [g p] (* p (m/abs (- 0.5 g)))) p-grid posterior2))

(defn loss [d] (reduce + (map (fn [g p] (* p (m/abs (- d g)))) p-grid posterior)))

;; Code 3.19
(apply min-key loss p-grid)

(clerk/vl
 (g/line-chart "loss" "decision" "expected proportional loss" p-grid (map loss p-grid)))

;; ----

;; ### 3.3 Sampled to simulate prediction

(def dummy-distr (r/distribution :binomial {:trials 2 :p 0.7}))

(map (partial r/pdf dummy-distr) [0 1 2])

(r/sample dummy-distr)

(r/->seq dummy-distr 10)

(def dummy-w (r/->seq dummy-distr 10000))

;; a table
(-> (frequencies dummy-w)
    (update-vals #(/ % 10000.0)))

;; ----

;; ### Figure 3.5

(def dummy-w2 (r/->seq (r/distribution :binomial {:trials 9 :p 0.7}) 10000))

(clerk/vl (g/frequencies-chart "Frequencies" "dummy water count" "Frequency" dummy-w2))

;; ----

;; ### Predicted observations for a single value `p=0.6` 

(defn make-dummy-chart [p]
  (g/frequencies-chart (str "p=" p) "" "" (r/->seq (r/distribution :binomial {:trials 9 :p p}) 10000)))

(clerk/vl (make-dummy-chart 0.6))

;; ----

;; ### Figures 3.6

(clerk/vl {:hconcat (map make-dummy-chart [0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9])})

;; Posterior predictive distribution for gathered samples (first grid search)

(clerk/vl
 (g/frequencies-chart "Predictive distribution" "number of water samples" "freq"
                      (map #(r/sample (r/distribution :binomial {:trials 9 :p %})) samples)))


;; ;; Posterior predictive distribution for gathered samples (second grid search)

(clerk/vl
 (g/frequencies-chart "Predictive distribution" "number of water samples" "freq"
                      (map #(r/sample (r/distribution :binomial {:trials 9 :p %})) samples2)))
