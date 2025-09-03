(ns bbnote.runall
  (:require [bbnote.run1 :as r1]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn process-pdfs-in-directory  
  "for every pdf in the directory call processing function" 
  [opts]
  (let [path (:path opts)
        dir (io/file path)]
    (println (str "Processing pdfs in directory: " path))
    (doseq [file (file-seq dir) 
            :when (and (.isFile file) (str/ends-with? (str/lower-case (.getName file)) ".pdf"))]
      (r1/process-one-pdf  {:filename (.getName file) :path path}))))



( comment
  (def path (io/file ".")) 
  (if (.isDirectory path)
    (doseq [file (file-seq path)
            ;;file (rs/process-one-pdf file) ;; remove spaces from file name
            :when (and ( .isFile file) (str/ends-with? (str/lower-case (.getName file)) ".pdf"))]
      (r1/process-one-pdf {:filename ( .getName file) :path path} ))
    (println (str "error: " " is not a directory"))
  ))