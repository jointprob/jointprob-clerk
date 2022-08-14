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

(defn relative-likelihood [x size p]
  (map #(dbinom x size %) p))

(defn standardize
  "make average of values in coll r = 1"
  [r]
  (let [average (/ (apply + r) (count r))]
    (map #(/ % average) r)))

(defn posterior-dis [x size]
  (let [p (map #(/ % 200) (range 0 201))
        relative-likelihood (map #(dbinom x size %) p)
        average-likelihood (/ (apply + relative-likelihood) 200)]
    (zipmap p (map #(/ % average-likelihood) relative-likelihood))))

(defn graph-posterior-dis [samples]
  (let [n (count samples)
        land (count (filter (partial = "L") samples))
        water (count (filter (partial = "W") samples))
        p (map #(/ % 200) (range 0 201))
        r-likelihood (relative-likelihood water n p)
        posterior (standardize r-likelihood)
        r-likelihood-graph (map #(hash-map :x %1 :y %2) p r-likelihood)
        posterior-graph (map #(hash-map :x %1 :y %2) p posterior)]
    (clerk/vl
      {
       :hconcat
       [
        {:title    (str "n = " n)
         :data     {:values
                    [{:x "W" :y (if (zero? n) 0 (/ water n))}
                     {:x "L" :y (if (zero? n) 0 (/ land n))}]}
         :mark     "bar",
         :encoding {:x {:field "x", :type "nominal", :axis {:labelAngle 0} :title "Land (L) or Water (W)?"},
                    :y {:field "y", :type "quantitative" :axis {:format "p"} :title "Percent of Sample"}}}
        {:title    (str "Relative Likelihood (n = " n ")")
         :data     {:values r-likelihood-graph}
         :mark     "line",
         :encoding {:x {:field "x", :type "quantitative", :axis {:labelAngle 0 :format "p"}
                        :title "% of world that is water"},
                    :y {:field "y" :type "quantitative" :title "probability"}}}
        {:title    (str "Posterior Probability (standardized) (n = " n ")")
         :data     {:values posterior-graph}
         :mark     "line",
         :encoding {:x {:field "x", :type "quantitative", :axis {:labelAngle 0 :format "p"}
                        :title "% of world that is water"},
                    :y {:field "y" :type "quantitative" :title "probability"}}}]})))