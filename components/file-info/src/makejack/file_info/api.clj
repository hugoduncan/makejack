(ns makejack.file-info.api
  "Info on a source file")


(defn dependencies
  [path]
  (impl/dependencies path))
