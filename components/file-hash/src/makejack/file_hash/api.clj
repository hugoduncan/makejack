(ns makejack.file-hash.api
  (:refer-clojure :exclude [hash])
  (:require
   [makejack.file-hash.impl :as impl]))

(defn hash
  "Return a file hash for the file at the given path."
  [path]
  (impl/md5-hash path))
