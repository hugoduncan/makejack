(ns makejack.file-info.impl
  (:require
   [makejack.file-hash.api :as file-hash]
   [makejack.file-info.namespace-deps :as namespace-deps]
   [makejack.filesystem.api :as filesystem]))

(defn dependencies
  ([path] (dependencies path #{:clj}))
  ([path features]
   (namespace-deps/dependencies path)))

(defn file-info [path]
  (assoc
   (dependencies path)
   :file-hash (file-hash/hash path)
   :last-modified (filesystem/last-modified path)))
