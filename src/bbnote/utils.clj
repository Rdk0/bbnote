(ns bbnote.utils
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [clojure.string :as str]))


(defn exit-program 
  "handle exit on error [str->str]"
  [status]
  (println "Omitting the step with status:" status)
  nil)


(defn doi-from-pdf 
  "retrieve doi as string from pdf text [str->str]"
  [pdf-file]
  (if-not (.exists (io/file pdf-file))
    (exit-program (str "the pdf file can't be found: " pdf-file));)
    (let [final-regex  "\\\\b(10[.][0-9]{4,}(?:[.][0-9]+)*/(?:(?![[\\\"]&'<>])\\\\S)+)\\\\b"
          command (str "pdftotext " pdf-file " - | grep -oP \"" final-regex "\" | head -n 1")
          {:keys [exit out err]} (sh "bash" "-c" command)] (if-not (= exit 0)
       ; (do
        (exit-program err)
        out))));)


(defn doi-to-bib   " retrieve bib based on doi  [str->str]" 
  [doi]
  (let [cleaned-doi (str/replace doi #"\n" "")]
    (sh "curl" (str "https://api.crossref.org/works/" cleaned-doi "/transform/application/x-bibtex"))))


(defn bib-to-author-year-journal  "from bib return pdf filename [str->str]"
  [bib]
  (let [author-re #"(?i)author=\{([^}]+?)\}"
        year-re #"(?i)year=\{([^}]+)\}"
        journal-re #"(?i)journal=\{([^}]+)\}"
        bib-string (:out bib)
        ; Extracting values
        author-match (re-find author-re bib-string)
        year-match (re-find year-re bib-string)
        journal-match (re-find journal-re bib-string)
        ;; Get the captured groups 
        author (when author-match (second author-match))
        year (when year-match (second year-match))
        journal (when journal-match (second journal-match))
        author-year-journal  {:author author
                              :year year
                              :journal journal}
        first-author (some-> (:author  author-year-journal)
                             (str/split #"\s+and\s+")
                             first
                             (str/split #"[,\s]+")
                             first
                             (str/replace #"[\{\}]" "")
                             (str/trim))]
    (str first-author "_" (:year author-year-journal) "_" (:journal author-year-journal) ".pdf")))


(defn make-file-md   "create md directory and .md file [str, str -> nil]"
  [path pdf bib]
  (if-not (str/blank? pdf)
    (let [filename-no-extn (subs pdf 0 (- (count pdf) 4))
          md-dir (str path "/md")
          md-file (str md-dir "/" filename-no-extn ".md")
          file-content  (clojure.string/replace (:out bib) "," "\n")
          file_content (str file-content "\n\n\n# Summary\n\n\n# Quotes and Data\n\n\n# Questions")]
      (io/make-parents (io/file md-file))
      (spit md-file file_content))
    (println "No PDF filename provided, skipping md file creation."))
  )


(defn rename-pdf  "rename pdf file to the new name [str,str -> nil]" 
  [original-filename new-filename]
  (let [old-file (io/file original-filename)
        new-file (io/file new-filename)]
    (if (.renameTo old-file new-file)
      (println "File renamed successfully!")
      (println "Failed to rename file."))))



    (comment
      (def pdf  "Trelford_2024_Cancers.pdf")
      (doi-to-bib (doi-from-pdf pdf))
      (bib-to-author-year-journal (doi-to-bib (doi-from-pdf pdf)))
       ;works => "Trelford_2024_Cancers.pdf"
      (def doi "10.3390/cancers16223726")
      (def bib (sh "curl" (str "https://api.crossref.org/works/" doi "/transform/application/x-bibtex")))
      ;; doi "10.3390/cancers16223726\n"
      ;; (doi-to-bib (doi-from-pdf pdf))
      ;; {:exit 0,
      ;;  :out
      ;;  " @article{Trelford_2024, title={LKB1 and STRADα Promote Epithelial Ovarian Cancer Spheroid Cell Invasion}, volume={16}, ISSN={2072-6694}, url={http://dx.doi.org/10.3390/cancers16223726}, DOI={10.3390/cancers16223726}, number={22}, journal={Cancers}, publisher={MDPI AG}, author={Trelford, Charles B. and Buensuceso, Adrian and Tomas, Emily and Valdes, Yudith Ramos and Hovey, Owen and Li, Shawn Shun-Cheng and Shepherd, Trevor G.}, year={2024}, month=nov, pages={3726} }\n",
      ;;  :err
      ;;  " % Total % Received % Xferd Average Speed Time Time Time Current\n Dload Upload Total Spent Left Speed\n\r 0 0 0 0 0 0 0 0 --:--:-- --:--:-- --:--:-- 0\r 0 0 0 0 0 0 0 0 --:--:-- --:--:-- --:--:-- 0\r100 472 0 472 0 0 719 0 --:--:-- --:--:-- --:--:-- 719\n"}
      (def bib {:exit 0,
                :out
                " @article{Trelford_2024, title={LKB1 and STRADα Promote Epithelial Ovarian Cancer Spheroid Cell Invasion}, volume={16}, ISSN={2072-6694}, url={http://dx.doi.org/10.3390/cancers16223726}, DOI={10.3390/cancers16223726}, number={22}, journal={Cancers}, publisher={MDPI AG}, author={Trelford, Charles B. and Buensuceso, Adrian and Tomas, Emily and Valdes, Yudith Ramos and Hovey, Owen and Li, Shawn Shun-Cheng and Shepherd, Trevor G.}, year={2024}, month=nov, pages={3726} }\n",
                :err
                " % Total % Received % Xferd Average Speed Time Time Time Current\n Dload Upload Total Spent Left Speed\n\r 0 0 0 0 0 0 0 0 --:--:-- --:--:-- --:--:-- 0\r 0 0 0 0 0 0 0 0 --:--:-- --:--:-- --:--:-- 0\r100 472 0 472 0 0 719 0 --:--:-- --:--:-- --:--:-- 719\n"})
      (rename-pdf "Trelford_2024_Cancers.pdf" (bib-to-author-year-journal bib)) ;works!
      ;(process-one-pdf  "Trelford_2024_Cancers.pdf" ".") ; out of bounds error
      (doi-from-pdf pdf); works => "10.3390/cancers16223726\n"
      (doi-to-bib doi); works => {:exit 0, :out " @article{Trelford_2024, ...", :err " ..."}
      ;(bib-to-author-year-journal (make-file-md "." pdf bib)(doi-to-bib doi)); make-file-md [path pdf bib]works => "T
      ;(subs pdf 0 (- (count author-year-journal) 3))
      (make-file-md "." pdf bib)
      )
