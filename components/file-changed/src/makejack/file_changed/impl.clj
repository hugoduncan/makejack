(ns makejack.file-changed.impl
  (:require
   [makejack.filesystem.api :as fs]
   [makejack.file-hash.api :as file-hash]
   [makejack.path.api :as path]))


(defn changed-file-hash
  "Return the changed file info if it has been modified."
  [file-info path-like]
  (let [k                       (str (path/as-path path-like))
        {:keys [hash modified]} (get file-info k)
        now-modified            (fs/last-modified path-like)]
    (when-not (= modified now-modified)
      (let [new-hash (file-hash/hash path-like)]
        (when-not (= hash new-hash)
          [k {:hash     new-hash
              :modified now-modified}])))))
