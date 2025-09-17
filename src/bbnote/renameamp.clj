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
    "for every pdf or md in the directory rename all files to replace &amp with underscore"
    [dir-path]
    (let [direc (io/file dir-path)]
      ( ->> (.listFiles direc)
       (filter (fn [file]
                 (let [file-name (.getName file)]
                   (and (clojure.string/includes? file-name "&") 
                        (or (clojure.string/ends-with? (clojure.string/lower-case file-name) ".pdf")
                            (clojure.string/ends-with? (clojure.string/lower-case file-name) ".md"))))))
       (run! (fn [file]
               (let [file-path (.getPath file)
                     new-name-path (remove-amp-in-string file-path)]
                 (println file-path  new-name-path)
                 (u/rename-pdf file-path new-name-path)))))))
  

(defn rename-pdfs-in-directories 
  "remove &amp from pdf filenames in main and md directories"
  [opts]
  (let [dir-path (:path opts)]
    (rename-pdfs-in-directory dir-path)
    (rename-pdfs-in-directory (str dir-path "/md")))
   
