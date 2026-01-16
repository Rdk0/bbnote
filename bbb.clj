(ns rad.bbb
  (:gen-class)
  (:require [tablecloth.api :as tc]
            [clojure.string :as str]
            [scicloj.tableplot.v1.hanami :as hanami]
            [scicloj.tableplot.v1.plotly :as plotly]
            ))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))


(comment
  (def filename  "/Users/rad/Documents/clojurecode/bbb/data/ci3c00150_si_002.csv")


  (def ds1 (tc/dataset filename  {:key-fn (comp keyword str/lower-case)}))

(tc/head ds1)
(tc/column-names ds1 )
;tc/column-names ds2)
(-> ds1 
    (tc/select-columns [:id :kpuu :kpuu_species :esol_kcal_mol])
    (tc/select-rows (fn [row] (.contains (:kpuu_species row) "mouse"))))


  (def dataset-with-log
    (tc/add-column ds1 :log10_kpuu
                   (fn [ds]
                     (map #(if (and % (pos? %))
                             (Math/log10 %)
                             nil)
                          (:kpuu ds)))))
  
  (-> dataset-with-log
      (tc/select-columns [:id :kpuu :kpuu_species :esol_kcal_mol :log10_kpuu])
      (tc/select-rows (fn [row] (.contains (:kpuu_species row) "mouse"))))


;; (-> dataset-with-log
;;     (plotly/layer-point {:=x :esol_kcal_mol
;;                          :=y :log10_kpuu}))

;; (-> dataset-with-log
;;     (plotly/base {:=title "log10(Kpuu) vs Esol (kcal/mol)"
;;                   :=width 700
;;                   :=height 500})
;;     (plotly/layer-point {:=x :esol_kcal_mol
;;                          :=y :log10_kpuu
;;                          :=mark-size 10}))

;;   ;; Color by project
;; (-> dataset-with-log
;;     (plotly/base {:=title "log10(Kpuu) vs Esol (kcal/mol)"})
;;     (plotly/layer-point {:=x :esol_kcal_mol 
;;                          :=y :log10_kpuu
;;                          :=color :project
;;                          :=mark-size 8}))

;; ;; Multiple layers example (line + points)
;; (-> dataset-with-log
;;     (plotly/base {:=title "log10(Kpuu) vs Esol"})
;;     (plotly/layer-line {:=x :esol_kcal_mol 
;;                         :=y :log10_kpuu})
;;     (plotly/layer-point {:=x :esol_kcal_mol 
;;                          :=y :log10_kpuu
;;                          :=mark-size 10}))

;; Note: If you don't specify :=x and :=y, plotly will try to use
;; :x and :y columns by default. Since your columns have different names,
;; you MUST specify them with the := prefix

;; ;; Filter out nil values before plotting
;; (-> dataset-with-log
;;     (tc/select-rows (fn [row] (some? (:log10_kpuu row))))
;;     (plotly/layer-point {:=x :esol_kcal_mol 
;;                          :=y :log10_kpuu}))

;; ;; Quick check: see the data before plotting
;; (-> dataset-with-log
;;     (tc/select-columns [:esol_kcal_mol :kpuu :log10_kpuu])
;;     (tc/head 10))


;; ;; (require '[scicloj.kindly-advice.v1.api :as kindly-advice])

;; (def my-plot
;;   (-> dataset-with-log
;;       (plotly/base {:=title "log10(Kpuu) vs Esol (kcal/mol)"
;;                     :=width 800
;;                     :=height 600})
;;       (plotly/layer-point {:=x :esol_kcal_mol
;;                            :=y :log10_kpuu
;;                            :=mark-size 8})))


;; Create the plot with color by species
(def my-plot
  (-> dataset-with-log
      (plotly/base {:=title "log10(Kpuu) vs Esol (kcal/mol)"
                    :=width 800
                    :=height 600})
      (plotly/layer-point {:=x :esol_kcal_mol 
                           :=y :log10_kpuu
                           :=color :kpuu_species  ; Color by species!
                           :=mark-size 8})))


;; (kindly-advice/write-html! my-plot "plot.html")

;; (require '[clayadapt.v1.api :as clayadapt])
;; (clayadapt/save-plotly! my-plot "plot.png")



;; Step 1: Create the base plot
(def my-plot-base
  (-> dataset-with-log
      (plotly/base {:=title "log10(Kpuu) vs Esol (kcal/mol)"
                    :=width 800
                    :=height 600})
      (plotly/layer-point {:=x :esol_kcal_mol 
                           :=y :log10_kpuu
                           :=color :kpuu_species
                           :=mark-size 8})))

;; Step 2: Render it and add shapes
(def my-plot2
  (let [render-fn (:kindly/f (meta my-plot-base) (:kindly/f my-plot-base))
        rendered (if render-fn (render-fn my-plot-base) my-plot-base)]
    (assoc-in rendered [:layout :shapes]
              [{:type "line"
                :x0 -17 :x1 -17
                :yref "paper"
                :y0 0 :y1 1
                :line {:color "red" :width 2 :dash "dash"}}
               {:type "line"
                :xref "paper"
                :x0 0 :x1 1
                :y0 (Math/log10 0.3) :y1 (Math/log10 0.3)
                :line {:color "blue" :width 2 :dash "dash"}}])))






(require '[clojure.data.json :as json])


(defn plotly->html
  "Convert a Plotly plot to a standalone HTML file"
  [plot-spec filename]
  (let [;; Access the rendering function using the kindly metadata
        render-fn (:kindly/f (meta plot-spec) (:kindly/f plot-spec))
        rendered (if render-fn
                   (render-fn plot-spec)
                   plot-spec)
        ;; Convert to JSON strings instead of Clojure pr-str
        data-json (json/write-str (:data rendered))
        layout-json (json/write-str (:layout rendered))
        html (str "<!DOCTYPE html>
<html>
<head>
    <meta charset='utf-8'>
    <script src='https://cdn.plot.ly/plotly-2.27.0.min.js'></script>
</head>
<body>
    <div id='plot' style='width:100%;height:100vh;'></div>
    <script>
        var data = " data-json ";
        var layout = " layout-json ";
        Plotly.newPlot('plot', data, layout, {responsive: true});
    </script>
</body>
</html>")]
    (spit filename html)
    (println "Plot saved to" filename)))



;; Save your plot
(plotly->html my-plot2 "plot2.html")


;; Calculate R² 

;; Step 1: Define the function
(defn calculate-r2
  [dataset x-col y-col]
  (let [clean-data (-> dataset
                       (tc/select-rows (fn [row]
                                         (and (some? (get row x-col))
                                              (some? (get row y-col))))))
        x-vals (vec (x-col clean-data))
        y-vals (vec (y-col clean-data))
        n (count x-vals)
        x-mean (/ (reduce + x-vals) n)
        y-mean (/ (reduce + y-vals) n)
        ss-tot (reduce + (map #(Math/pow (- % y-mean) 2) y-vals))
        numerator (reduce + (map #(* (- %1 x-mean) (- %2 y-mean)) x-vals y-vals))
        denominator (reduce + (map #(Math/pow (- % x-mean) 2) x-vals))
        b (/ numerator denominator)
        a (- y-mean (* b x-mean))
        y-pred (map #(+ a (* b %)) x-vals)
        ss-res (reduce + (map #(Math/pow (- %1 %2) 2) y-vals y-pred))
        r2 (- 1 (/ ss-res ss-tot))]
    {:r2 r2 :slope b :intercept a :n n}))

;; Step 2: Use it
(def correlation-stats
  (calculate-r2 dataset-with-log :esol_kcal_mol :log10_kpuu))

;; Step 3: View results
(println (format "R² = %.4f" (:r2 correlation-stats)))
(def correlation-stats
  (calculate-r2 dataset-with-log :esol_kcal_mol :log10_kpuu))

correlation-stats
;; => {:r2 0.xxx, :slope ..., :intercept ..., :n ...}

;; Print the results
(println (format "R² = %.4f" (:r2 correlation-stats)))
(println (format "Slope = %.4f" (:slope correlation-stats)))
(println (format "N = %d" (:n correlation-stats)))



;; Use original Kpuu values (not log10)
;; Create plot with log scale but showing original Kpuu values on y-axis
(def my-plot-log-scale
  (-> dataset-with-log
      (plotly/base {:=title "Kpuu vs Esol (kcal/mol)"
                    :=width 800
                    :=height 600})
      (plotly/layer-point {:=x :esol_kcal_mol 
                           :=y :kpuu  ; Use original Kpuu values, not log10!
                           :=color :kpuu_species
                           :=mark-size 8})))

;; Render and set y-axis to log scale with ONLY powers of 10
(def my-plot3
  (let [render-fn (:kindly/f (meta my-plot-log-scale) (:kindly/f my-plot-log-scale))
        rendered (if render-fn (render-fn my-plot-log-scale) my-plot-log-scale)]
    (-> rendered
        ;; Set y-axis to log scale
        (assoc-in [:layout :yaxis :type] "log")
        (assoc-in [:layout :yaxis :title] "Kpuu")
        ;; Remove intermediate ticks (2, 5, 20, 50, etc.)
        (assoc-in [:layout :yaxis :dtick] 1)  ; Only show powers of 10
        ;; Or use this for more control:
        ;; (assoc-in [:layout :yaxis :tickvals] [0.001 0.01 0.1 1 10 100])
        ;; Add reference lines at original values
        (assoc-in [:layout :shapes]
                  [{:type "line"
                    :x0 -17 :x1 -17
                    :yref "paper"
                    :y0 0 :y1 1
                    :line {:color "red" :width 2 :dash "dash"}}
                   {:type "line"
                    :xref "paper"
                    :x0 0 :x1 1
                    :y0 0.3 :y1 0.3  ; Use actual value 0.3, not log10(0.3)!
                    :line {:color "blue" :width 2 :dash "dash"}}]))))

;; Alternative: Specify exact tick values you want
(def my-plot-custom-ticks
  (let [render-fn (:kindly/f (meta my-plot-log-scale) (:kindly/f my-plot-log-scale))
        rendered (if render-fn (render-fn my-plot-log-scale) my-plot-log-scale)]
    (-> rendered
        (assoc-in [:layout :yaxis :type] "log")
        (assoc-in [:layout :yaxis :title] "Kpuu")
        ;; Specify EXACTLY which tick values to show
        (assoc-in [:layout :yaxis :tickvals] [0.01 0.1 1 10])
        (assoc-in [:layout :yaxis :ticktext] ["0.01" "0.1" "1" "10"])
        (assoc-in [:layout :shapes]
                  [{:type "line" :x0 -17 :x1 -17 :yref "paper" :y0 0 :y1 1
                    :line {:color "red" :width 2 :dash "dash"}}
                   {:type "line" :xref "paper" :x0 0 :x1 1 :y0 0.3 :y1 0.3
                    :line {:color "blue" :width 2 :dash "dash"}}]))))

(plotly->html my-plot3 "plot3.html")

  )