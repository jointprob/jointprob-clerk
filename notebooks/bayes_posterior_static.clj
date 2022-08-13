;; # Bayes' Posterior! 🌍
^{:nextjournal.clerk/visibility #{:hide-ns :hide}}
(ns ^:nextjournal.clerk/no-cache bayes-posterior-static
  (:require [nextjournal.clerk :as clerk]
            [dbinomial :as d]
            [nextjournal.clerk-slideshow :as slideshow]))


^{::clerk/viewer clerk/hide-result}
(clerk/add-viewers! [slideshow/viewer])


^{::clerk/viewer :hide-result}
(defonce random_samples (take 100 (repeatedly (fn [] (if (>= 0.6 (rand)) "W" "L")))))

; We want to know how much of the globe is covered by land.

; We (simulate) spin(ning) the globe and take a sample of the random place where it lands.

; **This is a slide show - use the left and right arrow keys to move back and forth and the escape key to see all slides.**
;
;----

; Before we have taken any samples, we have no knowledge of what portion of the world is land.
; All proportions of land vs water are equally likely.

(d/graph-posterior-dis [])

;----

; We spin the globe and get:

(take 1 random_samples)

; Now we have more information. We update our probabilities.

(d/graph-posterior-dis (take 1 random_samples))

;----

; Take another sample to get:

(take 2 random_samples)

(d/graph-posterior-dis (take 2 random_samples))

;----

; And another:
(take 3 random_samples)
(d/graph-posterior-dis (take 3 random_samples))

;----

; etc.....
(take 4 random_samples)
(d/graph-posterior-dis (take 4 random_samples))

;----
(take 5 random_samples)
(d/graph-posterior-dis (take 5 random_samples))

;----
(take 10 random_samples)
(d/graph-posterior-dis (take 10 random_samples))

;----
(take 15 random_samples)
(d/graph-posterior-dis (take 15 random_samples))

;----
(take 20 random_samples)
(d/graph-posterior-dis (take 20 random_samples))

;----
(take 40 random_samples)
(d/graph-posterior-dis (take 40 random_samples))

;----
(take 50 random_samples)
(d/graph-posterior-dis (take 50 random_samples))

;----
(take 100 random_samples)
(d/graph-posterior-dis (take 100 random_samples))

;----

; ## Clojure Implementation

; $$Pr(W,L|p)=\frac{(W+L)!}{W!L!}p^{W}(1-p)^L$$

;```clojure

;(defn dbinom [x size prob]
;  (* (/ (factorial size)
;        (* (factorial (- size x))
;           (factorial x)))
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


;(defn posterior-dis [x size]
;  (let [p (map #(/ % 200) (range 0 201))
;        relative-likelihood (map #(dbinom x size %) p)
;        average-likelihood (/ (apply + relative-likelihood) 200)]
;    (zipmap p (map #(/ % average-likelihood) relative-likelihood))))

;```
