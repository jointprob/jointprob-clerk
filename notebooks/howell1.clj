(ns howell1
  (:require [nextjournal.clerk :as clerk]
            [graphs :as g]
            [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]
            [kixi.stats.math :as km]
            [clojure.data.csv :as csv]
            [clojure.edn :as edn]
            [dbinomial :as d]
            ))

; # Demographic data from Kalahari !Kung San people collected by Nancy Howell


(def howell1-table (-> "data/Howell1.csv" ;; name of the file
                       slurp              ;; read the contents of the file
                       (csv/read-csv :separator \;)     ;; parse the result as csv data
                       clerk/use-headers)) ;; tell Clerk to use the first row as headers

(def howell1-table-with-adult-col
  {:rows
   (->> (:rows howell1-table)
        (map (partial map edn/read-string))
        (map (fn [[_ _ a _ :as all]] (concat all [(>= a 18)]))))
   :head (conj (:head howell1-table) "adult")})

(clerk/table howell1-table-with-adult-col)

; Convert data into array of maps for easy reference.
(def howell1-data (map #(zipmap (map keyword (:head howell1-table-with-adult-col)) %)
                       (:rows howell1-table-with-adult-col)))

(def point-chart-height-weight
  (hc/xform
   ht/point-chart
   :WIDTH 300
   :YSCALE {:zero false}
   :XSCALE {:zero false}
   :X "height"
   :Y "weight"
   :TOOLTIP ht/RMV
   :MTOOLTIP {:content "data"}
   :COLOR "male"))

(def point-chart-age-height
  (hc/xform
   ht/point-chart
   :WIDTH 300
   :X "age"
   :Y "height"
   :TOOLTIP ht/RMV
   :MTOOLTIP {:content "data"}
   :YSCALE {:zero false}
   :XSCALE {:zero false}
   :COLOR "male"))


(def adult-heights (filter :adult howell1-data))

(clerk/vl
 (hc/xform ht/vconcat-chart
           :VCONCAT [(hc/xform ht/hconcat-chart
                               :TITLE "All heights"
                               :DATA howell1-data
                               :HCONCAT [point-chart-height-weight
                                         point-chart-age-height])
                     (hc/xform ht/hconcat-chart
                               :TITLE "Adults heights (age >= 18)"
                               :DATA adult-heights
                               :HCONCAT [point-chart-height-weight
                                         point-chart-age-height])]))
(def adult-heights-bar-chart-data (map (fn [[height freq]] {:height height :freq freq})
                                       (frequencies (map #(Math/floor %) (map :height adult-heights)))))


(clerk/vl (hc/xform ht/bar-chart :DATA adult-heights-bar-chart-data
                    :X "height"
                    :Y "freq"))

(defn inclusive-range [start end steps]
  (let [step (/ (- end start) steps)]
    (range start (+ end step) step)))

(def grid (for [mu (inclusive-range 150 160 40)
                sd (inclusive-range 7 9 40)]
            {:mu mu :sd sd}))

(defn dnorm [x mu sd]
  ( / 
   (km/exp
    (/ (* -0.5 (km/sq (- x mu))) (km/sq sd)))
   (* (km/sqrt (* 2 km/PI)) sd)))

(clerk/vl (hc/xform ht/line-chart
                    :DATA (for [x (inclusive-range -1 1 200)]
                            {:x x :y (dnorm x 0 0.1)})))

(defn relative-likelihood-for-grid [grid x]
 (map (fn [{:keys [sd mu]} x](dnorm x mu sd)) grid (repeat x)))

(def relative-likelihood-for-grid-for-all-h
  (map #(->> %
             :height
             (relative-likelihood-for-grid grid)
             d/standardize) adult-heights))

(defn heat-map-graph 
  ([title data z-title]
  (hc/xform ht/heatmap-chart
            :TITLE title
            :WIDTH 400
            :HEIGHT 400
            :DATA data
            :X "mu"
            :XTYPE "nominal"
            :Y "sd"
            :YTYPE "nominal"
            :COLOR {:field "ll" :type "quantitative" :title z-title}
            :YSCALE {:zero false}
            :XSCALE {:zero false}
            :TOOLTIP [{:field "ll" :title z-title :type "quantitative"}
                      {:field "mu" :type "quantitative"}
                      {:field "sd" :type "quantitative"}]))
  ([title data]
   (heat-map-graph title data "Relative likelihood")))

;; We'll ignore the uniform prior for sd as it will have no effect on the posterior probability after
;; standardisation. Our prior for mu is a normal distribution with mean 178 and standard deviation 20.

(def prior (map (fn [{:keys [mu _]}] (dnorm mu 178.0 20)) grid))

(clerk/vl (heat-map-graph "Prior mu = Normal(178,20)"
                          (map #(assoc %1 :ll %2) grid prior)
                          "Prior"))

(clerk/vl
 (hc/xform ht/hconcat-chart
           :HCONCAT 
           (for [column (range 0 10)]
             (heat-map-graph (str "Relative likelihood of height "
                                  (inc column) " = "
                                  (:height (nth adult-heights column)))
                             (map
                              #(assoc %1 :ll %2)
                              grid
                              (nth relative-likelihood-for-grid-for-all-h column))))))

(def products-of-likelihoods
  (reductions #(->
                (map * %1 %2)
                d/standardize)
              relative-likelihood-for-grid-for-all-h))

(def posteriors
  (reductions #(->
                (map * %1 %2)
                d/standardize)
              prior
              relative-likelihood-for-grid-for-all-h))

(clerk/vl
 (hc/xform ht/vconcat-chart
           :VCONCAT 
           [(hc/xform ht/hconcat-chart
                      :HCONCAT
                      (for [no-of-heights (range 30 (count adult-heights) 30)]
                        (heat-map-graph (str "Relative likelihood of first " no-of-heights " heights.")
                                        (map
                                         #(assoc %1 :ll %2)
                                         grid
                                         (nth products-of-likelihoods no-of-heights)))))
            (hc/xform ht/hconcat-chart
                      :HCONCAT
                      (for [no-of-heights (range 30 (count adult-heights) 30)]
                        (heat-map-graph (str "Posterior after " no-of-heights " heights.")
                                        (map #(assoc %1 :ll %2)
                                             grid
                                             (nth posteriors no-of-heights))
                                        "Approximate probability density")))]))
(clerk/vl
 (heat-map-graph (str "Relative likelihood of all heights")
                 (map #(assoc %1 :ll %2)
                      grid
                      (last products-of-likelihoods))))

(clerk/vl
 (heat-map-graph (str "Posterior probability")
                 (map
                  #(assoc %1 :ll %2)
                  grid
                  (last posteriors))
                 "Approximate probability density"))