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
  (let [pdf-path (if (instance? java.io.File pdf-file)
                   (.getAbsolutePath pdf-file)
                   pdf-file)]
    (if-not (.exists (io/file pdf-path))
      (exit-program (str "the pdf file can't be found: " pdf-path))
      (let [;; Simplified DOI regex - matches pattern: 10.####/anything
            command (str "pdftotext '" pdf-path "' - | grep -oE '10\\.[0-9]{4,}/[^[:space:]]+' | head -n 1")
            {:keys [exit out err]} (sh "bash" "-c" command)]
        (println "DEBUG: DOI extraction command:" command)
        (println "DEBUG: Exit code:" exit)
        (println "DEBUG: Raw output:" (pr-str out))
        (if (= exit 0)
          (let [cleaned-doi (str/trim out)]
            (if (str/blank? cleaned-doi)
              (do
                (println "DEBUG: DOI extraction returned empty string")
                nil)
              (do
                (println "DEBUG: Found DOI:" cleaned-doi)
                cleaned-doi)))
          (do
            (println "DEBUG: DOI extraction command failed")
            (println "DEBUG: Error:" err)
            nil))))))


(defn doi-to-bib   " retrieve bib based on doi  [str->str]" 
  [doi]
  (if (nil? doi)
    nil
    (let [cleaned-doi (str/trim (str/replace doi #"\n" ""))]
      (println "DEBUG: Fetching bib for DOI:" cleaned-doi)
      (let [result (sh "curl" "-s" (str "https://api.crossref.org/works/" cleaned-doi "/transform/application/x-bibtex"))]
        (println "DEBUG: Bib fetch exit code:" (:exit result))
        (println "DEBUG: Bib fetch output length:" (count (:out result)))
        (if (and (= (:exit result) 0)
                 (not (str/includes? (:out result) "Resource not found")))
          result
          (do
            (println "DEBUG: Bib fetch failed or resource not found")
            nil))))))


(defn bib-to-author-year-journal  "from bib return pdf filename [str->str]"
  [bib]
  (if (nil? bib)
    nil
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
          journal (when journal-match (second journal-match))]
      (println "DEBUG: Author:" author)
      (println "DEBUG: Year:" year)
      (println "DEBUG: Journal:" journal)
      (if (or (nil? author) (nil? year) (nil? journal))
        (do
          (println "DEBUG: Missing required fields from bib")
          nil)
        (let [author-year-journal  {:author author
                                    :year year
                                    :journal journal}
              first-author (some-> (:author  author-year-journal)
                                   (str/split #"\s+and\s+")
                                   first
                                   (str/split #"[,\s]+")
                                   first
                                   (str/replace #"[\{\}]" "")
                                   (str/trim))
              filename (str first-author "_" (:year author-year-journal) "_" (:journal author-year-journal) ".pdf")]
          (println "DEBUG: Generated filename:" filename)
          filename)))))


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
    (println "DEBUG: Attempting rename from:" (.getAbsolutePath old-file))
    (println "DEBUG: Attempting rename to:" (.getAbsolutePath new-file))
    (if (.renameTo old-file new-file)
      (println "File renamed successfully!")
      (println "Failed to rename file."))))


(defn list-files-in-directory "list all files in the directory [str, str -> (java object)]"
  [path, ext]
  (let [dir (io/file path)]
    (->> (.listFiles dir)
         (filter #(and (.isFile %) (str/ends-with? (str/lower-case (.getName %)) ext)))
         (map #(.getName %))
         (doall))))



    (comment
      (def pdf  "Trelford_2024_Cancers.pdf")
      (doi-to-bib (doi-from-pdf pdf))
      (bib-to-author-year-journal (doi-to-bib (doi-from-pdf pdf)))
       ;works => "Trelford_2024_Cancers.pdf"
      (def doi "10.3390/cancers16223726")
      (def bib (sh "curl" (str "https://api.crossref.org/works/" doi "/transform/application/x-bibtex")))
      (def bib {:exit 0,
                :out
                " @article{Trelford_2024, title={LKB1 and STRADα Promote Epithelial Ovarian Cancer Spheroid Cell Invasion}, volume={16}, ISSN={2072-6694}, url={http://dx.doi.org/10.3390/cancers16223726}, DOI={10.3390/cancers16223726}, number={22}, journal={Cancers}, publisher={MDPI AG}, author={Trelford, Charles B. and Buensuceso, Adrian and Tomas, Emily and Valdes, Yudith Ramos and Hovey, Owen and Li, Shawn Shun-Cheng and Shepherd, Trevor G.}, year={2024}, month=nov, pages={3726} }\n",
                :err
                " % Total % Received % Xferd Average Speed Time Time Time Current\n Dload Upload Total Spent Left Speed\n\r 0 0 0 0 0 0 0 0 --:--:-- --:--:-- --:--:-- 0\r 0 0 0 0 0 0 0 0 --:--:-- --:--:-- --:--:-- 0\r100 472 0 472 0 0 719 0 --:--:-- --:--:-- --:--:-- 719\n"})
      (rename-pdf "Trelford_2024_Cancers.pdf" (bib-to-author-year-journal bib)) ;works!
      (doi-from-pdf pdf); works => "10.3390/cancers16223726\n"
      (doi-to-bib doi); works => {:exit 0, :out " @article{Trelford_2024, ...", :err " ..."}
      (make-file-md "." pdf bib)
      )
