(ns bbnote.run1
  (:require
   [clojure.string :as str]
   [bbnote.utils :as u]))


(defn process-one-pdf   
  "process one pdf file [str,str-> nil]:
   - get doi from pdf
   - get bib from doi
   - rename pdf based on bib info
   - make md file based on bib info"
  [opts]
  (let [path (:path opts)
        pdf  (:pdf opts)]
    (println "opts " opts)
    (if-let [doi (u/doi-from-pdf pdf)] ;check if doi is retrieved successfully
      (if-let [bib (u/doi-to-bib doi)]      ;check if bib is retrieved successfully
        (let [author-year-journal (u/bib-to-author-year-journal bib)]
          (if (not (str/blank? author-year-journal))
            (do
              (println "Processing file:" pdf)
              (println "Author-Year-Journal:" author-year-journal)
              (println "Bib content:" (:out bib))
              (println "Doi:" doi)
              (u/rename-pdf pdf author-year-journal)
              (u/make-file-md path author-year-journal bib))
            (println "No author, year, or journal found in bib. Skipping processing file" pdf)))
        (println " no bib retrieved - skipping processing file" pdf))
      (println " no doi found - skipping processing file" pdf))))

(comment
 ;pdf "Trelford_2024_Cancers.pdf"]
  )



