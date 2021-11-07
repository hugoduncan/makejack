(ns makejack.tasks.help
  (:require
   [makejack.target-doc.api :as target-doc]))

(defn ^{:doc-order 0 :params "[:target target-name]"} help
  "Show help

  Use :target to get detailed help on a specific target."
  ([params] (help params 'makejack.tasks.core))
  ([params ns-ref]
   ;; TODO: make this a function in target-doc
   (println)
   (println (target-doc/help-task params ns-ref))
   params))
