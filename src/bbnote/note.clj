(require '[babashka.cli :as cli])
(require '[bbnote.run1 :as r1])
(require '[bbnote.runall :as ra])
(require '[bbnote.run1usedoi :as rd])
(require '[bbnote.renameamp :as r&])
(require '[babashka.fs :as fs])
(require '[bbnote.removespace :as rs])


(defn dir-exists?
  [path]
  (fs/directory? path))


(def cli-opts
;  {:spec
  {:path {:alias :p
          :desc "path to directory with pdf files"
          :validate dir-exists?}
   :pdf {:alias :f
         :desc "pdf file name"
         :validate fs/exists?}
   :doi {:alias :d
         :desc "doi of the paper"}
         ;:coerce {:doi str}}
   :help {:alias :h
          :desc "print help"
          :default false}})


(defn help
  [_] 
  (println "------------------------------------------------------------------")
  (println "Commands:")
  (println "Usage: bbnote <command> <options>")
   (println "------------------------------------------------------------------")
  (println "examples:")
  (println "run1    \t -p dirname \t -f filename.pdf \t\t rename one pdf  and make md file")
  (println "runall  \t -p dirname \t \t \t \t \t rename all pdfs in the directory and make md files")
  (println "run1doi \t -p dirname \t -f filename.pdf \t -d doi  rename one pdf based on provided doi and make md file")
  (println "rename& \t -p dirname \t \t \t \t \t rename all pdfs with & in the name in the directory and the corresponding md files")
  (println "removesp \t -p dirname \t \t \t \t \t rename all pdfs in the directory replacing spaces with underscores")
  (println "------------------------------------------------------------------")
  (println "Options:") 
  (println "------------------------------------------------------------------")
  (println (cli/format-opts {:spec cli-opts})))


(def table
  [{:cmds ["run1"]     :fn #(r1/process-one-pdf            (:opts %)) :spec cli-opts}
   {:cmds ["runall"]   :fn #(ra/process-pdfs-in-directory  (:opts %)) :spec cli-opts}
   {:cmds ["run1doi"]  :fn #(rd/process-one-pdf-use-doi    (:opts %)) :spec cli-opts}
   {:cmds ["rename&"]  :fn #(r&/rename-pdfs-in-directories (:opts %)) :spec cli-opts}
   {:cmds ["removesp"] :fn #(rs/rename-pdfs-in-directory   (:opts %)) :spec cli-opts}
   {:cmds []           :fn   help}])


(cli/dispatch table *command-line-args* {:spec cli-opts :help help})

(comment
;bb  note.clj "run1" "-f" "Trelford_2024_Cancers.pdf" "-p" "."
  ) 
