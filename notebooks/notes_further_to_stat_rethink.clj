;; # Bayes' Posterior! 🌍
^{:nextjournal.clerk/visibility #{:hide-ns :hide}}
(ns ^:nextjournal.clerk/no-cache notes-further-to-stat-rethink
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
      (g/probability-dis "Likelihood of This W/L Permutation" "Pr(W,L|p)" coll-p
                         (d/r-likelihood-from-samples-simple coll-p samples))

      (g/probability-dis "Likelihood of Any Permutation" "Pr(W,L|p)" coll-p (d/r-likelihood-from-samples coll-p samples))
      (g/probability-dis "Posterior Probability (standardized)" "Pr(p|W,L)" coll-p
                         (->
                           (d/r-likelihood-from-samples coll-p samples)
                           d/standardize))]}))





^{::clerk/viewer clerk/hide-result}
(clerk/add-viewers! [slideshow/viewer])


^{::clerk/viewer :hide-result}
(def random_samples (take 1000 (repeatedly (fn [] (if (>= 0.6 (rand)) \W \L)))))

;# Bayesian Updating
;### another look at the binomial distribution formula.
;
; Problem : We want to know how much of the globe is covered by land.

; We (simulate) spin(ning) the globe and take a sample of the random place where it lands.

; **This is a slide show - use the left and right arrow keys to move back and forth and the escape key to see all slides.**
;
;----

;You will see a series of graphs followed on the next slide by a written description of the information that you can
;glean from the graphs. I suggest for each graph you might think carefully about putting into words all the information
;that you can read from the graph before going to the next slide.

;---
;
(clerk/vl
  (g/probability-dis "Likelihood" "probability" coll-p
                     (d/r-likelihood-from-samples-simple coll-p "")))

;----
(clerk/vl
  (g/probability-dis "Likelihood" "probability" coll-p
                     (d/r-likelihood-from-samples-simple coll-p "")))
;This graph tells you that we think that all proportions of water vs land are equally possible.

;In the book and video statistical rethinking the y-axis is labeled "probability".
;
;----

;Recall that:

;;$$
;;\text { Posterior }=\frac{\text { Probability of the data } \times \text { Prior }}{\text { Average probability of the data}}
;;$$
;;
;;$$
;;\operatorname{Pr}(p \mid W, L)=\frac{\operatorname{Pr}(W, L \mid p) \operatorname{Pr}(p)}{\operatorname{Pr}(W, L)}
;;$$
;;
;;$$
;;\operatorname{Pr}(W, L)=\int \operatorname{Pr}(W, L \mid p) \operatorname{Pr}(p) d p
;;$$

;----
;
;But here :

;$$
;;;\operatorname{Pr}(W, L)= 1
;$$

;and always for this thought experiment, the prior is:

;$$
;;;\operatorname{Pr}(p) = 1
;$$

;So:

;;;$$\operatorname{Pr}(p \mid W, L)=\frac{\operatorname{Pr}(W, L \mid p) \operatorname{Pr}(p)}{\operatorname{Pr}(W, L)}
;;; = {\operatorname{Pr}(W, L \mid p)}$$

;Here probability is both the probability of seeing this number of samples of water and land respectively. And it is
;also the posterior probability that is the same. (And this is also the prior probability. Since we have not yet seen any evidence.)

;----
;A question I have about this graph, if this is Pr(p|W,L), does it mean that both no water and an all
; water world have a probability of 1?

;This would seem to be impossible since these are mutually exclusive states.

;----

;>> A question I have about this graph, if this is Pr(p|W,L), does it mean that both no water and an all
; water world have a probability of 1?

;>> This would seem to be impossible since these are mutually exclusive states.

;Found this helpful to reflect on myself. What we are actually seeing in this graph is that there is equal probability
;for the **range** of
;proportions of water from 0% to 100%.
;
;On this graph if we take the area under the graph for any range of proportions of
;water this will be the probability of this **range** of proportions of water.
;
;Eg. for this graph the entire area under the graph is 1 which of course means there is 100% probability that the
;proportion of water is somewhere between 0 and 100%. 50% probability that the proportion of water is between 0 and 50%
;or 50 and 100%, etc...
;
;OR we can compare two points on the graph to get a relative likelihood.

;----

; We spin the globe and say we get say:

"W"

(clerk/vl
  (g/probability-dis "Likelihood" "Pr(W,L|p)" coll-p
                     (d/r-likelihood-from-samples coll-p "W")))

;----

"W"

(clerk/vl
  (g/probability-dis "Likelihood" "Pr(W,L|p)" coll-p
                     (d/r-likelihood-from-samples coll-p "W")))

;This graph tells you that we think that there is no chance of an all water world.
;
;Why this straight line? We can see obviously that there is zero chance of a no water world now since we have evidence
;there is at least some water. And the chance of seeing this sample of water on a random toss goes up in proportion to
;p (the proportion of the globe that is covered in water) until it reach 100% for a 100% water covered world.
;
;Note this is a graph of Pr(W,L|p) what would Pr(p|W,L) look like for one "W"?

;----

(clerk/vl
  (g/probability-dis "Posterior Distribution" "Pr(p|W,L)" coll-p
                     (->(d/r-likelihood-from-samples coll-p "W")
                        d/standardize)))

;----

;Conversely if our first sample was a "L" then Pr(W,L|p) would be:

(clerk/vl
  (g/probability-dis "Likelihood" "Pr(W,L|p)" coll-p
                     (d/r-likelihood-from-samples coll-p "L")))


;----

;And the probability distribution of Pr(p|W,L) will be:

"L"

(clerk/vl
  (g/probability-dis "Posterior Distribution" "Pr(p|W,L)" coll-p
                     (->(d/r-likelihood-from-samples coll-p "L")
                        d/standardize)))

;----

;For more samples we just keep on taking the product of these straight line likelihoods of seeing either a "L" sample
;or a "W" sample.

;Then we might expect the formula for the probability of W,L given p to be:

; $$Pr(W,L|p)=p^{W}(1-p)^L$$
;
;But the correct formula is
;$$Pr(W,L|p)=\frac{(W+L)!}{W!L!}p^{W}(1-p)^L$$
;
;So where is this from?
;$$\frac{(W+L)!}{W!L!}$$

;----

; Whereas the following term is the probability of any one path through the garden of forking data of finding W water samples and
; L land samples eg. for 1 land and 1 water one sequence would be "WL":
;
; $$p^{W}(1-p)^L$$
;
; This term is the number of such paths, the number of possible sequences of W water samples and L land samples
;
; $$\frac{(W+L)!}{W!L!}$$
;
; eg. for 1 land and 1 water there are two possible sequences. "WL" and "LW".

;----

;All paths have the same probability $$p^{W}(1-p)^L$$ but there are $$\frac{(W+L)!}{W!L!}$$ paths so we multiply the terms
;together to get the probability of any possible sequence of water and land samples instead of just the one particular sequence.

;----
; Take another sample to get:

(take 2 random_samples)

(graph-posterior-dis (take 2 random_samples))

;----
;
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

