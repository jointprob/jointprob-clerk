(ns howell1
  (:require [nextjournal.clerk :as clerk]
            [graphs :as g]
            [kixi.stats.core :as ks]
            [clojure.data.csv :as csv]))

; # Demographic data from Kalahari !Kung San people collected by Nancy Howell


(def howell1-table (-> "data/Howell1.csv" ;; name of the file
    slurp              ;; read the contents of the file
    (csv/read-csv :separator \;)     ;; parse the result as csv data
    clerk/use-headers)) ;; tell Clerk to use the first row as headers

(clerk/table howell1-table)

(def howell1-data (map #(zipmap (map keyword (:head howell1-table)) %)
                       (:rows howell1-table)))

(clerk/vl {:hconcat [(g/point-chart
                      "Height vs Weight for the !Kung People"
                      "Height" "Weight"
                      (map :height howell1-data) (map :weight howell1-data))
                     (g/point-chart
                      "Age vs Height for the !Kung People"
                      "Age" "Height"
                      (map :age howell1-data) (map :height howell1-data))]})