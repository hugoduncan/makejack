(ns makejack.files.impl
  (:require
   [makejack.dag.api :as dag]
   [makejack.file-hash.api :as file-hash]
   [makejack.file-info.api :as file-info]
   [makejack.filesystem.api :as filesystem]
   [makejack.path.api :as path]))

(defn add-file
  "Add file to the info-map."
  [info-map path]
  (let [p        (path/path path)
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
                  (:requires info))
        info-map (reduce
                  (fn [info-map dep-ns]
                    (update info-map :ns-dag dag/add-edge p-ns dep-ns))
                  info-map
                  (:imports info))]
    info-map))

(defn add-files
  "Add files to the info-map."
  [info-map paths]
  (reduce
   add-file
   info-map
   paths))

(defn remove-files
  "Remove files from the info map."
  [info-map paths]

  )

(defn file-changed [info-map path]
  (let [file-info         (-> info-map :path->file-info (get path))
        new-last-modified (filesystem/last-modified path)
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

(defn add-path-info
  [info-map path]
  (add-files
   info-map
   (filterv filesystem/file? (filesystem/list-paths path))))

(defn info-map
  "Return a new file info map."
  [params paths]
  (reduce
   add-path-info
   {:ns-dag (dag/graph)}
   (mapv #(path/path (:dir params ".") %) paths)))

(defn topo-namespaces
  "Return namespaces in topological order."
  [info-map]
  (let [ns->path (:ns->path info-map)
        nses     (keys ns->path)
        ns-dag   (:ns-dag info-map)]
    (assert (some? ns-dag))
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
