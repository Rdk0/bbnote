(ns bbnote.renameamp
   (:require
    [bbnote.utils :as u]
    [clojure.java.io :as io]
    [clojure.string :as str]))
  
  
  (defn remove-amp-in-string [word]
    (str/replace word #"&amp"  "_"))
  
  
  (defn rename-pdfs-in-directory 
    "for every pdf in the directory remove_&amp"
    [dir-path]
    (let [dir (io/file dir-path)]
       (doseq [file (file-seq dir)
              :when (and (.isFile file) (str/ends-with? (str/lower-case (.getName file)) ".pdf"))]
        (u/rename-pdf (.getName file) (remove-amp-in-string  (.getName file))))))

  
(defn rename-pdfs-in-directories 
  "remove &amp from pdf filenames in main and md directories"
  [opts]
  (let [dir-path (:path opts)]
    (rename-pdfs-in-directory [dir-path])
    (rename-pdfs-in-directory [(str dir-path "/MD")])))
   
