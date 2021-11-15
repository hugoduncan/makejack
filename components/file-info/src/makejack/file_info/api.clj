(ns makejack.file-info.api
  "Info on a source file"
  (:require
   [makejack.file-info.impl :as impl]))

(defn file-info
  "Return a map of file-changed time, hash and dependencies."
  [path]
  (impl/file-info path))
