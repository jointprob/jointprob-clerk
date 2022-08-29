(ns graphs)

(defn land-or-water [n land water]
  {:title    (str "n = " n)
   :data     {:values
              [{:x "W" :y (if (zero? n) 0 (/ water n))}
               {:x "L" :y (if (zero? n) 0 (/ land n))}]}
   :mark     "bar",
   :encoding {:x {:field "x", :type "nominal", :axis {:labelAngle 0} :title "Land (L) or Water (W)?"},
              :y {:field "y", :type "quantitative" :axis {:format "p"} :title "Percent of Sample"}}})

(defn probability-dis [title x-title p y-values]
  {:title    title
   :data     {:values (map #(hash-map :x %1 :y %2) p y-values)}
   :mark     "line",
   :encoding {:x {:field "x", :type "quantitative", :axis {:labelAngle 0 :format "p"}
                  :title "% of world that is water"},
              :y {:field "y" :type "quantitative" :title x-title}}})

(defn line-chart [title x-title y-title x-values y-values]
  {:title title
   :data {:values (map #(hash-map :x %1 :y %2) x-values y-values)}
   :mark {:type "line" :point {:filled false :fill "white"}}
   :encoding {:x {:field "x" :type "quantitative" :title x-title}
              :y {:field "y" :type "quantitative" :title y-title}}})

(defn point-chart [title x-title y-title x-values y-values]
  {:title title
   :data {:values (map #(hash-map :x %1 :y %2) x-values y-values)}
   :mark :point
   :encoding {:x {:field "x" :type "quantitative" :title x-title}
              :y {:field "y" :type "quantitative" :title y-title}}})
