(ns makejack.files.impl
  (:require
   [babashka.fs :as fs]
   [makejack.dag.api :as dag]
   [makejack.file-hash.api :as file-hash]
   [makejack.file-info.api :as file-info]))

(defn add-file
  "Add file to the info-map."
  [info-map path]
  (let [p        (fs/path path)
        info     (-> (file-info/file-info p)
                     (assoc :path (str p)))
        p-ns     (:namespace info)
        info-map (cond-> info-map
                   true (update :path->file-info assoc p info)
                   p-ns (update :ns->path assoc p-ns p)
                   p-ns (update :path->ns assoc p p-ns))
        info-map (reduce
                  (fn [info-map dep-ns]
                    (update info-map :ns-dag dag/add-edge p-ns dep-ns))
                  info-map
                  (:require info))
        info-map (reduce
                  (fn [info-map dep-ns]
                    (update info-map :ns-dag dag/add-edge p-ns dep-ns))
                  info-map
                  (:import info))]
    info-map))

(defn add-files
  "Add files to the info-map."
  [info-map file-paths]
  (reduce
   add-file
   info-map
   file-paths))

(defn remove-files
  "Remove files from the info map."
  [info-map paths]

  )

(defn file-changed [info-map path]
  (let [file-info         (-> info-map :path->file-info (get path))
        new-last-modified (fs/file-time->millis (fs/last-modified-time path))
        new-file-hash     (when (> new-last-modified (:last-modified file-info))
                            (file-hash/hash path))]
    (if (= new-file-hash (:file-hash file-info))
      info-map
      (assoc-in
       info-map [:path->file-info path]
       (-> file-info
           (assoc :file-hash new-file-hash))))))


(defn files-changed
  "Trigger rebuilding file info based on changes in the give paths.
  Return the set of dependent files."
  [info-map paths]

  )


(defn files-in-dir [path]
  (when (fs/directory? path)
    (filterv fs/regular-file? (fs/glob path "**"))))

(defn add-dir
  [info-map dir-path]
  []
  (add-files info-map (files-in-dir dir-path)))

(defn info-map
  "Return a new file info map."
  [params dir-paths]
  (reduce
   add-dir
   {:ns-dag (dag/graph)}
   (mapv #(if (fs/relative? %)
            (fs/path (:dir params ".") %)
            %)
         dir-paths)))

(defn topo-namespaces
  "Return namespaces in topological order."
  [info-map]
  (let [ns->path (:ns->path info-map)
        nses     (keys ns->path)
        ns-dag   (:ns-dag info-map)]
    (assert (some? ns-dag))
    (assert (some? ns->path) info-map)
    (->> (dag/topo-sort (:child-edges ns-dag) (:parent-edges ns-dag) nses)
         (filterv ns->path))))

(defn top-level-nses
  "Return (topologically) top-level namespace."
  [info-map]
  (->> info-map
       :ns->path
       keys
       (remove (:parent-edges (:ns-dag info-map)))
       vec))
