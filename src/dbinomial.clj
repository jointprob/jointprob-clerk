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