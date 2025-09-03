(ns bbnote.removespace
  (:require 
   [bbnote.utils :as u]
   [clojure.java.io :as io]
   [clojure.string :as str]))


(defn remove-spaces-in-string [word]
  (str/replace word #" "  "_"))


(defn rename-pdfs-in-directory "for every pdf in the directory remove_spaces" 
  [opts]
   (let [path (:path opts)
         dir (io/file path)]
      (doseq [file (file-seq dir)
              :when (and (.isFile file) (str/ends-with? (str/lower-case (.getName file)) ".pdf"))]
        (u/rename-pdf (.getName file) (remove-spaces-in-string  (.getName file))))))


(comment 
  (def dir-path ".")
  (def dir (io/file dir-path)) 
  (doseq [file (file-seq dir)
          :when (and (.isFile file) (str/ends-with? (str/lower-case (.getName file)) ".pdf"))]
    (u/rename-pdf (.getName file) (remove-spaces-in-string  (.getName file))))
  )