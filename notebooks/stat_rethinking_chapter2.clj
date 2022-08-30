^{:nextjournal.clerk/visibility #{:hide-ns :hide}}
(ns stat-rethinking-chapter2
  (:require [nextjournal.clerk :as clerk]
            [graphs :as g]
            [dbinomial :as d]
            [nextjournal.clerk-slideshow :as slideshow]))
;# Review of Chapter 2 of Statistical Rethinking "Small Worlds and Large Worlds"
;## Quantifying certainty / uncertainty.
;- Small Worlds and Large Worlds
;    - Christopher Columbus analogy suggests two things simultaneously.
;        - models are 'small world' in that they can sometimes / often be wrong like Columbus.
;        - a model is a small representation of the large world
;    - map and territory is another analogy
;- "Bayesian models have some advantages in this regard, as they have reasonable claims to optimality: No alternative model could make better use of the information in the data and support better decisions, assuming the small world is an accurate description of the real world."
;- "The way that Bayesian models learn from evidence is arguably optimal in the small world. When their assumptions approximate reality, they also perform well in the large world."
;- "Bayesian inference is really just counting and comparing of possibilities."
;    - possible realities as a "Garden of Forking Paths" (Borges' short story)
;    - these are pruned down by evidence which tells us which paths correspond to the observed world.
;- Summary of Bayesian inference in my own words (please correct):
;    - our model parameters and our model tells us the probability of different paths in the garden of forking data.
;    - given each observation about reality, we can make firmer and firmer inferences about the unobserved model parameters as we go deeper into the garden of forking data.
;    - our model and the likelihood distribution of the parameters we have found will then allow us to make predictions about what we will observe next.
;- Two examples of this process of inference are given in chapter 2:
;    - discrete and continuous parameters:
;        - for discrete unobserved parameters - the number of black balls in a bag of four balls.
;        - for a continuous unobserved parameter - the proportion of water covering a globe.
;    - both examples infer from a single discrete rather than continuous observation ie. number of black balls pulled from bag, number of land samples.
;- both examples use "ignorance" priors
;    - "This is sometimes known as the **PRINCIPLE OF INDIFFERENCE**: When there is no reason to say that one conjecture is more plausible than another, weigh all of the conjectures equally. This book does not use nor endorse “ignorance” priors. As we’ll see in later chapters, the structure of the model and the scientific context always provide information that allows us to do better than ignorance."
;    - For the proportion of water on the globe for example we are saying:
;        - if in this world we are examining we know nothing about how much water is on the globe what can we know from the observed random samples?
;-
