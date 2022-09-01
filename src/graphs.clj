(ns graphs)

(defn land-or-water [n land water]
  {:title    (str "n = " n)
   :data     {:values
              [{:x "W" :y (if (zero? n) 0 (/ water n))}
               {:x "L" :y (if (zero? n) 0 (/ land n))}]}
   :mark     "bar",
   :encoding {:x {:field "x", :type "nominal", :axis {:labelAngle 0} :title "Land (L) or Water (W)?"},
              :y {:field "y", :type "quantitative" :axis {:format "p"} :title "Percent of Sample"}}})

(defn- build-data
  [xs ys]
  (map #(hash-map :x %1 :y %2) xs ys))

(defn probability-dis [title x-title p y-values]
  {:title    title
   :data     {:values (build-data p y-values)}
   :mark     "line",
   :encoding {:x {:field "x", :type "quantitative", :axis {:labelAngle 0 :format "p"}
                  :title "% of world that is water"},
              :y {:field "y" :type "quantitative" :title x-title}}})

(defn line-chart [title x-title y-title x-values y-values]
  {:title title
   :data {:values (build-data x-values y-values)}
   :mark :line
   :encoding {:x {:field "x" :type "quantitative" :title x-title}
              :y {:field "y" :type "quantitative" :title y-title}}})

(defn point-chart [title x-title y-title x-values y-values]
  {:title title
   :data {:values (build-data x-values y-values)}
   :mark {:type :point :opacity 0.5}
   :encoding {:x {:field "x" :type "quantitative" :title x-title}
              :y {:field "y" :type "quantitative" :title y-title}}})

(defn distribution-with-area
  [title x-title y-title x-values y-values transform]
  {:title title
   :data {:values (build-data x-values y-values)}
   :encoding {:x {:field "x" :type "quantitative" :title x-title}
              :y {:field "y" :type "quantitative" :title y-title}}
   :layer [{:mark :area
            :transform [{:filter (merge transform {:field "x"})}]}
           {:mark :line}]})
