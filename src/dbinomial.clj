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

(defn posterior-dis [x size]
  (let [p (map #(/ % 200) (range 0 201))
        relative-likelihood (map #(dbinom x size %) p)
        average-likelihood (/ (apply + relative-likelihood) 200)]
    (zipmap p (map #(/ % average-likelihood) relative-likelihood))))

(defn graph-posterior-dis [samples]
  (let [n (count samples)
        land (count (filter (partial = "L") samples))
        water (count (filter (partial = "W") samples))
        p-dis-values (map #(hash-map :p (first %) :likelihood (second %)) (posterior-dis water n))]
    (clerk/vl
      (let [p-dist-graph {:title    (str "Probability Distribution (n = " n ")")
                          :data     {:values p-dis-values}
                          :mark     "line",
                          :encoding {:x {:field "p", :type "quantitative", :axis {:labelAngle 0 :format "p"}
                                         :title "% of world that is water"},
                                     :y {:field "likelihood" :type "quantitative" :title "posterior probability"}}}]
        {:hconcat
         [
          {:title    (str "n = " n)
           :data     {:values
                      [{:x "W" :y (if (zero? n) 0 (/ water n))}
                       {:x "L" :y (if (zero? n) 0 (/ land n))}]}
           :mark     "bar",
           :encoding {:x {:field "x", :type "nominal", :axis {:labelAngle 0} :title "Land (L) or Water (W)?"},
                      :y {:field "y", :type "quantitative" :axis {:format "p"} :title "Percent of Sample"}}}
          p-dist-graph]}))))