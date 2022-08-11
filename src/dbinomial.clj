(ns dbinomial)

(defn exp
  "a ^ b"
  [a b]
  (reduce *' (repeat b a)))

(defn a!
  "factorial of a" [a]
  (apply *' (range 1 (inc a))))

(defn dbinom [x size prob]
  (* (/ (a! size)
        (* (a! (- size x))
           (a! x)))
     (exp prob x)
     (exp (- 1 prob) (- size x))))