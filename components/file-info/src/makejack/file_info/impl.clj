(ns makejack.file-info.impl
  (:require
   [babashka.fs :as fs]
   [makejack.file-hash.api :as file-hash]
   [makejack.namespace.api :as namespace]))

(defn file-info [path]
  (merge
   (namespace/dependencies path)
   {:file-hash     (file-hash/hash path)
    :last-modified (fs/file-time->millis (fs/last-modified-time path))}))
