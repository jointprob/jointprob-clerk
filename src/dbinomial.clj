(ns dbinomial
  (:require [nextjournal.clerk :as clerk]))

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

(defn binomial-dis-for [samples]
  (let [n (count samples)
        found-land (count (filter (partial = "W") samples))
        binomial-distribution
        (map
          (fn [r] (let [p (/ r 200)]
                    (hash-map :x p :y (dbinom found-land n p))))
          (range 0 201))]
    (clerk/vl
      (let [p-dist-graph {:title    (str "Probability Distribution (n = " n ")")
                          :data     {:values binomial-distribution}
                          :mark     "line",
                          :encoding {:x {:field "x", :type "quantitative", :axis {:labelAngle 0} :title "probability of water"},
                                     :y {:field "y" :type "quantitative" :title "posterior probability"}}}]
        {:hconcat
         [
          {:title    (str "n = " n)
           :data     {:values
                      [{:x "W" :y (if (zero? n) 0 (/ (- found-land n) n))}
                       {:x "L" :y (if (zero? n) 0 (/ found-land n))}]}
           :mark     "bar",
           :encoding {:x {:field "x", :type "nominal", :axis {:labelAngle 0}},
                      :y {:field "y", :type "quantitative"}}}
          p-dist-graph]}))))