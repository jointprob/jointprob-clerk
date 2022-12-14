^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns ^:nextjournal.clerk/no-cache making-the-model-go
  (:require [nextjournal.clerk :as clerk]
            [graphs :as g]
            [nextjournal.clerk-slideshow :as slideshow]
            [fastmath.random :as r]
            [fastmath.protocols]))

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
         '[fastmath.core :as m]
         '[fastmath.kernel :as k])

(m/use-primitive-operators)

;; A `fastmath.random` namespace contains a set of functions connected to the randomness. In this notebook I use the following:

;; * `r/randval` - to select randomly one of two provided values with given probability. I use it to generate our data.
;; * `r/distibution` - to create a distibution
;; * `r/pdf` - to calculate likelihood
;; * `r/drand` - to get random floating point (double) number
;; * `r/grand` - to get random number from normal distribution
;; * `m/slice-range` - to get evenly distributed points from given range.
;; * `k/kernel-density` - to construct pdf from samples

;; ----

;; # The data

;; First we need our data. To simulate the experiment described in the book I build the sequence of `:W` (water) and `:L` (land) keywords with given probability of selecting a water (`water-probability`).

(def water-probability (r/drand 0.55 0.65))

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
    (fn data-likelihood ^double [^double p]
      (if (<= 0.0 p 1.0)
        (let [binomial (make-model trials p)]
          (r/pdf binomial water-count))
        0.0))))

;; Let's see how our likelihood look like for our data, but different (randomly selected) `p` values. 

(def ps (repeatedly 100 r/drand))
(defn data-likelihoods-n [n] (map (make-model-likelihood (take n data)) ps))

^{::clerk/visibility :hide}
(clerk/vl {:hconcat [(g/point-chart "Model likelihood for 20 samples"
                                    "parameter p"                                   
                                    "likelihood"
                                    ps (data-likelihoods-n 20))
                     (g/point-chart "Model likelihood for 200 samples"
                                    "parameter p"
                                    "likelihood"
                                    ps (data-likelihoods-n 200))
                     (g/point-chart "Model likelihood for 2000 samples"
                                    "parameter p"
                                    "likelihood"
                                    ps (data-likelihoods-n 2000))]})

;; ----

;; ## Prior

;; The Prior is an information about the parameter `p` we have. We know for sure that it's between `0.0` and `1.0` and we know that it should be a distribution. Let's assume it's an uniform distribution.

(def prior-distribution (r/distribution :uniform-real {:lower 0.0 :upper 1.0}))

;; ----

;; ## Unnormalized posterior

;; The posterior is just a multiplication of likelihood and prior `pdf` values for given data and `p` divided by evidence. This should give us a distribution of `p`. The evidence is usually unknown, so I make unnormalized posterior at the beginning.

(defn make-unnormalized-posterior [likelihood prior]
  (fn [p] (* ^double (likelihood p) (r/pdf prior p))))

;; Let's build 3 different posterior distributions for different number of gathered data.

(def unnormalized-posterior-20
  (make-unnormalized-posterior (make-model-likelihood (take 20 data)) prior-distribution))
(def unnormalized-posterior-200
  (make-unnormalized-posterior (make-model-likelihood (take 200 data)) prior-distribution))
(def unnormalized-posterior-2000
  (make-unnormalized-posterior (make-model-likelihood (take 2000 data)) prior-distribution))

;; ----

;; ## Evidence

;; We can try to estimate the evidence by numerically calculate a integral. In our case (having only one parameter) it's quite simple.

(defn evidence
  ^double [unnormalized-posterior]
  (let [values (map unnormalized-posterior (range 0.0 1.0 0.001))]
    (reduce m/fast+ (map (fn [^double v] (* 0.001 v)) values))))

^{::clerk/visibility :hide}
(clerk/example
 (evidence unnormalized-posterior-20)
 (evidence unnormalized-posterior-200)
 (evidence unnormalized-posterior-2000))

;; I manually calculated the evidence for `[:W :W :W :L :L]` which is `1/6`. The following result is estimated quite well.

(evidence (make-unnormalized-posterior (make-model-likelihood [:W :W :W :L :L]) prior-distribution))

;; ----

;; ## Posterior

;; Now we are ready to create real posterior function with calculated evidence.

(defn make-posterior
  [unnormalized-posterior]
  (let [pdata (evidence unnormalized-posterior)]
    (fn ^double [^double p] (/ ^double (unnormalized-posterior p) pdata))))

(def posterior-20 (make-posterior unnormalized-posterior-20))
(def posterior-200 (make-posterior unnormalized-posterior-200))
(def posterior-2000 (make-posterior unnormalized-posterior-2000))

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
        sum (double (reduce m/fast+ values))]
    (map (fn [^double v] (/ v sum)) values)))

^{::clerk/visibility :hide}
(clerk/vl {:hconcat [(g/point-chart "Posterior for 20 samples"
                                    "Parameter p"
                                    "Posterior"
                                    grid-100 (grid-values-normalized unnormalized-posterior-20))
                     (g/point-chart "Posterior for 200 samples"
                                    "Parameter p"
                                    "Posterior"
                                    grid-100 (grid-values-normalized unnormalized-posterior-200))
                     (g/point-chart "Posterior for 2000 samples"
                                    "Parameter p"
                                    "Posterior"
                                    grid-100 (grid-values-normalized unnormalized-posterior-2000))]})

;; `fastmath` allows to build distribution from values and probabilities

(def grid-distribution (r/distribution :enumerated-real {:data grid-100
                                                       :probabilities (grid-values-normalized posterior-200)}))

(r/pdf grid-distribution (nth grid-100 50))

;; ----

;; ## Quadratic approximation

;; The main idea here is to model a posterior with Normal distribution. We need to figure out mean and standard deviation. From the code of `quap` function it's done this way:

;; * `J` - is the negative log of the `posterior`, this is our target function for optimization.
;; * `mean` - is the minimum of the `J` function.
;; * `standard deviation` - is the sqrt of the inverse of second derivative (inverse of the Hessian) of the `J` (see [here](https://onlinelibrary.wiley.com/doi/pdf/10.1002/9780470824566.app1)).

;; To find a minimum, let's use `optimization` namespace from the `fastmath` library.

(require '[fastmath.optimization :as o])

(defn make-J [posterior] (fn ^double [^double v] (- (m/log (posterior v)))))

(defn mean ^double [posterior]
  (ffirst (o/minimize :gradient (make-J posterior) {:bounds [[0.0 1.0]] :initial [0.5]})))

;; To calculate second derivative we can use finite difference method.

(defn stddev ^double [posterior ^double mean]
  (m/sqrt (/ (let [J (make-J posterior)
                   ^double fx+h (J (+ mean 0.001))
                   ^double fx (J mean)
                   ^double fx-h (J (- mean 0.001))]
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

;; The procedure is rougly as follows: `p` values are created from a random walk, jumping here and there (step is randomly selected from Normal distribution). Decision about jumping to the new `p` is based on the proportion between posterior at the new (proposed) `p` and the old value. And it is also made randomly.

;; I propose slighly different implementation (without wrapping around the boundaries which is presented in the book).

(defn mcmc-step
  [posterior ^double old-p]
  (let [new-p (r/grand old-p 0.1)
        ^double q0 (posterior old-p)
        ^double q1 (posterior new-p)]
    (if (and (not (zero? q1)) ;; do not jump when zero posterior
             (< (r/drand) (/ q1 q0))) new-p old-p)))

;; Here are samples after running above, the initial value is arbitrary (`0.5`)

(take 10 (iterate (partial mcmc-step posterior-200) 0.5))

;; Now we can build a density function out of the samples using kernel density estimation

(defn mcmc-density
  [posterior]
  (let [samples (iterate (partial mcmc-step posterior) 0.5)]
    (k/kernel-density :gaussian (->> samples
                                     (drop 500) ;; skip some first samples
                                     (take-nth 3) ;; skip some samples
                                     (take 100000)))))

(def kde-20 (mcmc-density posterior-20))
(def kde-200 (mcmc-density posterior-200))
(def kde-2000 (mcmc-density posterior-2000))

(kde-20 0.5)

^{::clerk/visibility :hide}
(clerk/vl {:hconcat [(g/line-chart "Density of MCMC (data size=20)"
                                   "Parameter p"
                                   "Density"
                                   grid-100 (map kde-20 grid-100))
                     (g/line-chart "Density of MCMC (data size=200)"
                                   "Parameter p"
                                   "Density"
                                   grid-100 (map kde-200 grid-100))
                     (g/line-chart "Density of MCMC (data size=2000)"
                                   "Parameter p"
                                   "Density"
                                   grid-100 (map kde-2000 grid-100))]})

;; ----

;; # Practice

;; Solutions for selected excercises

;; ----

;; ## 2M1

^{::clerk/viewer :hide-result}
(def data-2m1 [[:W :W :W] [:W :W :W :L] [:L :W :W :L :W :W :W]])

(defn grid-posterior
  ([data] (grid-posterior data prior-distribution))
  ([data prior] (-> data make-model-likelihood (make-unnormalized-posterior prior) grid-values-normalized)))

(clerk/vl {:hconcat [(g/point-chart (str "Data: " (data-2m1 0)) "Parameter p" "Probability" grid-100 (grid-posterior (data-2m1 0)))
                     (g/point-chart (str "Data: " (data-2m1 1)) "Parameter p" "Probability" grid-100 (grid-posterior (data-2m1 1)))
                     (g/point-chart (str "Data: " (data-2m1 2)) "Parameter p" "Probability" grid-100 (grid-posterior (data-2m1 2)))]})

;; ----

;; ## 2M2

(def custom-prior
  (reify fastmath.protocols/DistributionProto
    (pdf [_ v] (if (< ^double v 0.5) 0.0 2.0))))

(clerk/vl {:hconcat [(g/point-chart (str "Data: " (data-2m1 0)) "Parameter p" "Probability" grid-100 (grid-posterior (data-2m1 0) custom-prior))
                     (g/point-chart (str "Data: " (data-2m1 1)) "Parameter p" "Probability" grid-100 (grid-posterior (data-2m1 1) custom-prior))
                     (g/point-chart (str "Data: " (data-2m1 2)) "Parameter p" "Probability" grid-100 (grid-posterior (data-2m1 2) custom-prior))]})

