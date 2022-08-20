(ns dbinomial)

(defn exp
  "a ^ b"
  [a b]
  (reduce *' (repeat b a)))

(defn factorial
  "factorial of a" [a]
  (apply *' (range 1 (inc a))))

(defn dbinom [x size prob]
  (* (/ (factorial size)
        (* (factorial (- size x))
           (factorial x)))
     (exp prob x)
     (exp (- 1 prob) (- size x))))

(defn relative-likelihood [x size coll-p]
  "Calculate the relative likelihood for a collection of p."
  (map #(dbinom x size %) coll-p))

(defn bayesian-binary-update
  "Given prior probability in collection prior, which is a collection of evenly spaced probability samples over a
  range 0 to 1 inclusive and a binary variable found update to a new relative-likelihood distribution."
  [found prior]
  (let [n (count prior)
        update-if-found (map #(/ % n) (range 0 (inc n)))    ; range from 0 to 1 inclusive with n elements
        update-if-not-found (map #(/ % n) (range n -1 -1))]
    (map * prior (if found update-if-found update-if-not-found))))

(defn relative-likelihood-simple [x size coll-p]
  (let [n (count coll-p)
        uniform-prior (repeat n 1)
        times-found x
        times-not-found (- size x)]
    (->                                                     ; repeatedly update depending on times-found and times-not-found :
      (reduce (fn [last-step _] (bayesian-binary-update true last-step)) uniform-prior (repeat times-found 1))
      (#(reduce (fn [last-step _] (bayesian-binary-update false last-step)) % (repeat times-not-found 1))))))

(defn standardize
  "make average of values in coll r = 1"
  [r]
  (let [average (/ (apply + r) (count r))]
    (map #(/ % average) r)))

(defn count-land-or-water [samples]
  (let [n (count samples)
        land (count (filter (partial = "L") samples))
        water (count (filter (partial = "W") samples))]
    [n land water]))

(defn r-likelihood-from-samples [coll-p samples]
  (let [[n _ water] (count-land-or-water samples)]
    (relative-likelihood water n coll-p)))

(defn r-likelihood-from-samples-simple [coll-p samples]
  (let [[n _ water] (count-land-or-water samples)]
    (relative-likelihood-simple water n coll-p)))
