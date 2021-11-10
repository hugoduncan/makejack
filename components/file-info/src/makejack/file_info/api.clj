(ns makejack.file-info.api
  "Info on a source file"
  #_(:require
     [makejack.file-info.impl :as impl]))

(defn dependencies
  [path]
  ((requiring-resolve 'makejack.file-info.impl/dependencies) path))

(defn file-info
  [path]
  ((requiring-resolve 'makejack.file-info.impl/file-info) path))
