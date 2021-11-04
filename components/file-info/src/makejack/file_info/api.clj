(ns makejack.file-info.api
  "Info on a source file"
  (:require
   [makejack.file-info.impl :as impl]))

(defn dependencies
  [path]
  (impl/dependencies path))

(defn file-info
  [path]
  (impl/file-info path))
