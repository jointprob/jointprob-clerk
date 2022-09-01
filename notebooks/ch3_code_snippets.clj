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


