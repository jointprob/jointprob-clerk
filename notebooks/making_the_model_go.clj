^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns ^:nextjournal.clerk/no-cache making-the-model-go
  (:require [nextjournal.clerk :as clerk]
            [graphs :as g]
            [nextjournal.clerk-slideshow :as slideshow]
            [fastmath.random :as r]))

^{::clerk/viewer :hide-result
  ::clerk/visibility :hide}
(clerk/add-viewers! [slideshow/viewer])

;; # Making the model go!

;; ### How to go from Bayesian formula to the concrete code.

;; In this presentation I build all three methods of finding the posterior:
;;
;; * Grid approximation
;; * Quadratic approximation
;; * Markov chain Monte Carlo (MCMC)

;; *This is a slide show - use the left and right arrow keys to move back and forth and the escape key to see all slides.*

;; ----

;; # Preparation

;; I use [fastmath](https://github.com/generateme/fastmath) library for my implementation. 

(require '[fastmath.random :as r]
         '[fastmath.core :as m])

;; A `fastmath.random` namespace contains a set of functions connected to the randomness. In this notebook I use the following:

;; * `r/randval` - to select randomly one of two provided values with given probability. I use it to generate our data.
;; * `r/distibution` - to create a distibution
;; * `r/pdf` - to calculate likelihood
;; * `r/drand` - to get random floating point (double) number
;; * `m/slice-range` - to get evenly distributed points from given range.

;; ----

;; # The data

;; First we need our data. To simulate the experiment described in the book I build the sequence of `:W` (water) and `:L` (land) keywords with given probability of selecting a water (`water-probability`).

(def water-probability (r/drand 0.45 0.75))

(def data (repeatedly #(r/randval water-probability :W :L)))

;; *Here is the fact number 1: we should treat the data as it was random, always.*

;; ----

;; # Distributions

;; The heart of the Bayesian inference is the fact that we model our data in terms of probability distributions (the data is random). A distribution is a function which gives us an information how probable (or plausible, like in the book) our data is. We plug in the data entry and as the outcome we get some measure.

;; This measure has many names in the literature, it can be: plausibility, probability, density, score, likelihood, observation, pdf, pmf, and probably some more depending on type of the distribution and context.

;; Here is the example of Normal (Gaussian) distribution with `mean=1.0` and `standard deviation=0.2`

(def normal-distribution (r/distribution :normal {:mu 1.0 :sd 0.2}))

;; Let's verify how plausible are values `1.0`, `2.0` and `-1.0`:

^{::clerk/visibility :hide}
(clerk/example
 (r/pdf normal-distribution 1.0)
 (r/pdf normal-distribution 2.0)
 (r/pdf normal-distribution -1.0))

;; ----

;; # The Model

;; We need to construct our model in terms of probability distribution. The book author selects a binomial distribution, which is probably the best one we can have here.

(defn make-model [trials p] (r/distribution :binomial {:trials trials :p p}))

;; For given number of trials, and given parameter `p`, we can find plausibility of all possible data (ie. such with no `:W`, with one `:W`, with two `:W` etc... up to `20`)

(def model-20-trials (make-model 20 water-probability))
(def all-possible-water-counts (map #(r/pdf model-20-trials %) (range 20)))

^{::clerk/visibility :hide}
(clerk/vl (g/line-chart "Model plausibility for different data"
                        "Number of water hits"
                        "Plausibility"
                        (range) all-possible-water-counts))

;; As you can see, when the proportion of the water to all samples is near `water-probability`, the value of the plausibility is higher.

;; *Here is the fact number 2: Bayesian inference doesn't help you to select a model, you have to do this by your own.*

;; ----

;; # The Bayesian formula

;; Now let's look at the Baysian formula and try to understand the building blocks.

;;$$
;;\begin{array}{rrrrr}
;;\text{Posterior}=\frac{\text {Likelihood} \times \text {Prior}}{\text {Evidence}}
;; & or &
;;\operatorname{Pr}(p \mid W,L)=\frac{\operatorname{Pr}(W,L \mid p) \operatorname{Pr}(p)}{\operatorname{Pr}(W,L)}
;; & or &
;;\operatorname{P}(\mathit{parameters} \mid \mathit{data})=\frac{\operatorname{P}(\mathit{data} \mid \mathit{parameters}) \operatorname{P}(\mathit{parameters})}{\operatorname{P}(\mathit{data})}
;; \end{array}
;;$$

;; Each of the formula components answers some question:

;; * Likelihood - what is the plausibility of the data for given model and for given parametrization of the model
;; * Prior - what we know about the distribution of parametrization of the model
;; * Posterior - what is the distribution of parametrization of the model for given data
;; * Evidence - what is the plausibility of the data for given model averaged (integrated) by all possible parametrization.

;; The last one, the Evidence, is just a constant value for given data. The bad thing is: it's hard to calculate (or impossible to calculate), the good thing is: you don't need this in the most cases.

;; *Here is the fact number 3: The Bayesian inference is about finding the distribution of the model parameters.*

;; ----

;; ## Likelihood

;; Actually we need a function which returns likelihood for given data under the parameter `p`. It's opposite to the model example. We know our data, we don't know `p`.

(defn make-model-likelihood [data]
  (let [trials (count data)
        water-count (count (filter #{:W} data))]
    (fn data-likelihood [p]
      (let [binomial (make-model trials p)]
        (r/pdf binomial water-count)))))

;; Let's see how our likelihood look like for our data, but different (randomly selected) `p` values. 

(def ps (repeatedly 100 r/drand))
(defn data-likelihoods-n [n] (map (make-model-likelihood (take n data)) ps))

^{::clerk/visibility :hide}
(clerk/vl {:hconcat [(g/line-chart "Model likelihood for 20 samples"
                                   "parameter p"                                   
                                   "likelihood"
                                   ps (data-likelihoods-n 20))
                     (g/line-chart "Model likelihood for 200 samples"
                                   "parameter p"
                                   "likelihood"
                                   ps (data-likelihoods-n 200))
                     (g/line-chart "Model likelihood for 2000 samples"
                                   "parameter p"
                                   "likelihood"
                                   ps (data-likelihoods-n 2000))]})

;; ----

;; ## Prior

;; The Prior is an information about the parameter `p` we have. We know for sure that it's between `0.0` and `1.0` and we know that it should be a distribution. Let's assume it's an uniform distribution.

(def prior-distribution (r/distribution :uniform-real {:lower 0.0 :upper 1.0}))

;; ----

;; ## Posterior

;; The posterior is just a multiplication of likelihood and prior `pdf` values for given data and `p` divided by evidence. This should give us a distribution of `p`. The evidence is usually unknown, so I make unnormalized posterior.

(defn make-unnormalized-posterior [likelihood prior]
  (fn [p] (* (likelihood p) (r/pdf prior p))))

;; Let's build 3 different posterior distributions for different number of gathered data.

(def posterior-20 (make-unnormalized-posterior (make-model-likelihood (take 20 data)) prior-distribution))
(def posterior-200 (make-unnormalized-posterior (make-model-likelihood (take 200 data)) prior-distribution))
(def posterior-2000 (make-unnormalized-posterior (make-model-likelihood (take 2000 data)) prior-distribution))

;; ----

;; # Inference algorithms

;; ----

;; ## Grid approximation

;; The first algorithm just scans the whole range for parameters and calculates posterior values.

(def grid-100 (m/slice-range 0.0 1.0 100))

;; Since our posterior is unnormalized we should find an evidence and normalize to posterior. Our posterior forms a discrete distribution which `pmf` is defined only in grid points.

(defn grid-values-normalized
  [posterior]
  (let [values (map posterior grid-100)
        sum (reduce + values)]
    (map #(/ % sum) values)))

^{::clerk/visibility :hide}
(clerk/vl {:hconcat [(g/point-chart "Posterior for 20 samples"
                                    "Parameter p"
                                    "Posterior"
                                    grid-100 (grid-values-normalized posterior-20))
                     (g/point-chart "Posterior for 200 samples"
                                    "Parameter p"
                                    "Posterior"
                                    grid-100 (grid-values-normalized posterior-200))
                     (g/point-chart "Posterior for 2000 samples"
                                    "Parameter p"
                                    "Posterior"
                                    grid-100 (grid-values-normalized posterior-2000))]})

;; ----

;; ## Quadratic approximation

;; The main idea here is to model a posterior with Normal distribution. We need to figure out mean and standard deviation. From the code of `quap` function it's done this way:

;; * `J` - is the negative log of the `posterior`, this is our target function for optimization.
;; * `mean` - is the minimum of the `J` function.
;; * `standard deviation` - is the sqrt of the inverse of second derivative (inverse of the Hessian) of the `J` (see [here](https://onlinelibrary.wiley.com/doi/pdf/10.1002/9780470824566.app1)).

;; To find a minimum, let's use `optimization` namespace from the `fastmath` library.

(require '[fastmath.optimization :as o])

(defn make-J [posterior] (fn [v] (- (m/log (posterior v)))))

(defn mean [posterior] (ffirst (o/minimize :brent (make-J posterior) {:bounds [[0.0 1.0]] :initial [0.5]})))


;; To calculate second derivative we can use finite difference method.

(defn stddev [posterior mean] (m/sqrt (/ (let [J (make-J posterior)
                                            fx+h (J (+ mean 0.001))
                                            fx (J mean)
                                            fx-h (J (- mean 0.001))]
                                        (/ (- (+ fx+h fx-h) (* 2.0 fx))
                                           (* 0.001 0.001))))))

(defn make-normal-pdf [posterior]
  (let [mu (mean posterior)
        normal (r/distribution :normal {:mu mu :sd (stddev posterior mu)})]
    (partial r/pdf normal)))

^{::clerk/visibility :hide}
(clerk/vl {:hconcat [(g/line-chart "Normal distribution for posterior-20"
                                   "Parameter p"
                                   "Posterior"
                                   grid-100 (map (make-normal-pdf posterior-20) grid-100))
                     (g/line-chart "Normal distribution for posterior-200"
                                   "Parameter p"
                                   "Posterior"
                                   grid-100 (map (make-normal-pdf posterior-200) grid-100))
                     (g/line-chart "Normal distribution for posterior-2000"
                                   "Parameter p"
                                   "Posterior"
                                   grid-100 (map (make-normal-pdf posterior-2000) grid-100))]})

;; ----

;; ## MCMC

;; Markov chain Monte Carlo algorithm do not produce a `pdf` function for a posterior distribution. Instead it produces a samples from the posterior.
