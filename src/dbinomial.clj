(ns dbinomial)

(defn exp
  "a ^ b"
  [a b]
  (reduce *' (repeat b a)))

(defn factorial
  "factorial of a" [a]
  (apply *' (range 1 (inc a))))

#_(defn n-of-permutations [x n]
    (/ (factorial n)
       (* (factorial (- n x))
          (factorial x))))

;; shouldn't produce overflow
(defn n-of-permutations [x n]
  (/ (reduce *' (range n x -1))
     (factorial (- n x))))

(defn dbinom [x n prob]
  (* (n-of-permutations x n)
     (exp prob x)
     (exp (- 1 prob) (- n x))))

(defn relative-likelihood [x n coll-p]
  "Calculate the relative likelihood for a collection of p."
  (map #(dbinom x n %) coll-p))

(defn bayesian-binary-update[found prior coll-p]
  (let [update-if-found coll-p    ; range from 0 to 1 inclusive with size elements
        update-if-not-found (map #(- 1 %) coll-p)]
    (map *' prior (if found update-if-found update-if-not-found))))

(defn relative-likelihood-simple [x n coll-p]
  "size is no of samples of distributions - assumes evenly spaced samples from 0 to 1 inclusive."
  (let [uniform-prior (repeat (count coll-p) 1)
        times-found x
        times-not-found (- n x)]
    (-> ; repeatedly update depending on times-found and times-not-found :
      (reduce (fn [last-step _] (bayesian-binary-update true last-step coll-p)) uniform-prior (repeat times-found 1))
      (#(reduce (fn [last-step _] (bayesian-binary-update false last-step coll-p)) % (repeat times-not-found 1))))))

(defn standardize
  "make average of values in coll r = 1"
  [r]
  (let [average (/ (apply + r) (count r))]
    (map #(/ % average) r)))

(defn count-land-or-water [samples]
  (let [n (count samples)
        land (count (filter (partial = \L) samples))
        water (count (filter (partial = \W) samples))]
    [n land water]))

(defn r-likelihood-from-samples [coll-p samples]
  (let [[n _ water] (count-land-or-water samples)]
    (relative-likelihood water n coll-p)))

(defn r-likelihood-from-samples-simple [coll-p samples]
  (let [[n _ water] (count-land-or-water samples)]
    (relative-likelihood-simple water n coll-p)))
