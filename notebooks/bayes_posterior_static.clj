;; # Bayes' Posterior! 🌍
^{:nextjournal.clerk/visibility #{:hide-ns :hide}}
(ns ^:nextjournal.clerk/no-cache bayes-posterior-static
  (:require [nextjournal.clerk :as clerk]
            [graphs :as g]
            [dbinomial :as d]
            [nextjournal.clerk-slideshow :as slideshow]))

^{::clerk/viewer clerk/hide-result}
(def coll-p (map #(/ % 200) (range 0 201)))

^{::clerk/viewer clerk/hide-result}
(defn graph-posterior-dis [samples]
  (clerk/vl
    {
     :hconcat
     [(apply g/land-or-water (d/count-land-or-water samples))
      (g/probability-dis "Relative Likelihood" "Pr(W,L|p)" coll-p (d/r-likelihood-from-samples coll-p samples))
      (g/probability-dis "Posterior Probability (standardized)" "Pr(p|W,L)" coll-p
                         (->
                           (d/r-likelihood-from-samples coll-p samples)
                           d/standardize))]}))


^{::clerk/viewer clerk/hide-result}
(clerk/add-viewers! [slideshow/viewer])


^{::clerk/viewer :hide-result}
(def random_samples (into []  (take 100 (repeatedly (fn [] (if (>= 0.6 (rand)) \W \L))))))

; We want to know how much of the globe is covered by land.

; We (simulate) spin(ning) the globe and take a sample of the random place where it lands.

; **This is a slide show - use the left and right arrow keys to move back and forth and the escape key to see all slides.**
;
;----

; Before we have taken any samples, we have no knowledge of what portion of the world is land.
; All proportions of land vs water are equally likely.

(graph-posterior-dis [])

;----

; We spin the globe and get:

(take 1 random_samples)
;
;; Now we have more information. We update our probabilities.
;
(graph-posterior-dis (take 1 random_samples))

;----

; Take another sample to get:

(take 2 random_samples)

(graph-posterior-dis (take 2 random_samples))

;----

; And another:
(take 3 random_samples)
(graph-posterior-dis (take 3 random_samples))

;----

; etc.....
(take 4 random_samples)
(graph-posterior-dis (take 4 random_samples))

;----
(take 5 random_samples)
(graph-posterior-dis (take 5 random_samples))

;----
(take 10 random_samples)
(graph-posterior-dis (take 10 random_samples))

;----
(take 15 random_samples)
(graph-posterior-dis (take 15 random_samples))

;----
(take 20 random_samples)
(graph-posterior-dis (take 20 random_samples))

;----
(take 40 random_samples)
(graph-posterior-dis (take 40 random_samples))

;----
(take 50 random_samples)
(graph-posterior-dis (take 50 random_samples))

;----
(take 100 random_samples)
(graph-posterior-dis (take 100 random_samples))

;----

; ## Clojure Implementation

; $$Pr(W,L|p)=\frac{(W+L)!}{W!L!}p^{W}(1-p)^L$$

;```clojure

;(defn n-of-permutations [x size]
;  (/ (factorial size)
;     (* (factorial (- size x))
;        (factorial x))))
;
;(defn dbinom [x size prob]
;  (* (n-of-permutations x size)
;     (exp prob x)
;     (exp (- 1 prob) (- size x))))

;```


;----
;
;$$
;\text { Posterior }=\frac{\text { Probability of the data } \times \text { Prior }}{\text { Average probability of the data}}
;$$
;
;$$
;\operatorname{Pr}(p \mid W, L)=\frac{\operatorname{Pr}(W, L \mid p) \operatorname{Pr}(p)}{\operatorname{Pr}(W, L)}
;$$
;
;$$
;\operatorname{Pr}(W, L)=\int \operatorname{Pr}(W, L \mid p) \operatorname{Pr}(p) d p
;$$

;----

;```clojure

;(defn standardize
;  "make average of values in coll r = 1"
;  [r]
;  (let [average (/ (apply + r) (count r))]
;    (map #(/ % average) r)))

;```
