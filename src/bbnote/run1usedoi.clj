(ns bbnote.run1usedoi
  (:require [bbnote.utils :as u]))

(defn process-one-pdf-use-doi 
  "turn-key function for processing one pdf file [str,str-> nil]"
  [opts]
  (let [{:keys [path pdf doi] } opts] ;path (:path opts)
         ;pdf  (:pdf opts)
         ;doi (:doi opts)]
    (println "opts " opts)
   (if-let [bib (u/doi-to-bib doi)]      ;check if bib is retrieved successfully
     (let [author-year-journal (u/bib-to-author-year-journal bib)]
       (u/rename-pdf (str path "/" pdf) (str path "/" author-year-journal))
       (u/make-file-md path (str path "/" author-year-journal) bib))
     (println " no bib:  skipping processing file " pdf))))



