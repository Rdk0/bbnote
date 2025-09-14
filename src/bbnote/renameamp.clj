(ns bbnote.renameamp
   (:require
    [bbnote.utils :as u]
    [clojure.java.io :as io]
    [clojure.string :as str]))
  
  
  (defn remove-amp-in-string [word]
    (str/replace word #"&amp"  "_"))
  
  
  ;; (defn rename-pdfs-in-directory 
  ;;   "for every pdf in the directory remove_&amp"
  ;;   [dir-path]
  ;;   (let [direc (io/file dir-path)]
  ;;      (doseq [file (file-seq direc)
  ;;              :when (and (.contains (.getName file) "&amp") (.isFile file) (or (str/ends-with? (str/lower-case (.getName file)) ".pdf") (str/ends-with? (str/lower-case (.getName file)) ".md")) )]
  ;;        (println (.getName file) (remove-amp-in-string  (.getName file)) )
  ;;        (u/rename-pdf (.getName file) (remove-amp-in-string  (.getName file))))))

  (defn rename-pdfs-in-directory
    "for every pdf or md in the directory remove &amp"
    [dir-path]
    (let [direc (io/file dir-path)]
      (->> (file-seq direc)
           (filter (fn [file]
                     (and (.isFile file)
                          (clojure.string/includes? (.getName file) "&") ; use clojure.string/includes?
                          (or (clojure.string/ends-with? (clojure.string/lower-case (.getName file)) ".pdf")
                              (clojure.string/ends-with? (clojure.string/lower-case (.getName file)) ".md")))))
           (run! (fn [file]
                   (let [new-name (remove-amp-in-string (.getName file))]
                     (println (.getName file) new-name)
                     (u/rename-pdf (.getName file) new-name)))))))
  

(defn rename-pdfs-in-directories 
  "remove &amp from pdf filenames in main and md directories"
  [opts]
  (let [dir-path (:path opts)]
    (rename-pdfs-in-directory dir-path)))
   ; (rename-pdfs-in-directory (str dir-path "/md"))))
   
