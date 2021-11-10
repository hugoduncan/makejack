(ns makejack.file-changed.impl
  (:require
   [babashka.fs :as fs]
   [makejack.file-hash.api :as file-hash]))


(defn changed-file-hash
  "Return the changed file info if it has been modified."
  [file-info path]
  (let [k                       (str (fs/path path))
        {:keys [hash modified]} (get file-info k)
        now-modified            (-> path
                                    fs/last-modified-time
                                    fs/file-time->millis)]
    (when-not (= modified now-modified)
      (let [new-hash (file-hash/hash path)]
        (when-not (= hash new-hash)
          [k {:hash     new-hash
              :modified now-modified}])))))
