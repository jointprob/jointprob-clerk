(ns dbinomial-test
  (:require [clojure.test :refer :all])
  (:require [dbinomial :refer :all]))

(deftest exp-test
  (is (= (exp 2.0 4.0) 16.0)))


(deftest factorial-test
  (are [a fac] (= fac (factorial a))
               0 1
               1 1
               2 2
               3 6))



(deftest dbinom-test
  (is (= (dbinom 6 9 0.5) 0.1640625)))
