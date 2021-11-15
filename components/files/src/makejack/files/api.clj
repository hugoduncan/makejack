(ns makejack.files.api
  "Info on all files."
  (:require
   [makejack.files.impl :as impl]))

(defn files-in-dir
  "Return all file paths under the given root path."
  [path]
  (impl/files-in-dir path))

(defn info-map
  "Return a new file info map."
  [params paths]
  (impl/info-map params paths))

(defn add-files
  "Add files to the info-map."
  [info-map paths]
  (impl/add-files info-map paths))

(defn remove-files
  "Remove files from the info map"
  [info-map paths]
  (impl/remove-files info-map paths))

(defn files-changed
  "Trigger rebuilding file info based on changes in the give paths"
  [info-map paths]
  (impl/files-changed info-map paths))

(defn topo-namespaces
  "Return namespaces in topological order."
  [info-map]
  (impl/topo-namespaces info-map))

(defn top-level-nses
  "Return (topologically) top-level namespace."
  [info-map]
  (impl/top-level-nses info-map))
