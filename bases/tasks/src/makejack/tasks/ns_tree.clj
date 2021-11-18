(ns makejack.tasks.ns-tree
  (:require
   [babashka.fs :as fs]
   [clojure.pprint :as pprint]
   [makejack.defaults.api :as defaults]
   [makejack.deps.api :as deps]
   [makejack.files.api :as files]))

(defn ns-tree
  "Return namespace tree info."
  [params]
  (let [basis    (defaults/basis params)
        info-map (->> basis
                      deps/lift-local-deps
                      defaults/paths
                      (mapv #(fs/relativize
                              (fs/absolutize (fs/path (:dir params ".")))
                              (fs/absolutize (fs/path %))))
                      (files/info-map params)
                      )]
    (println "Unreferenced namespaces"
             (files/top-level-nses info-map))
    (pprint/pprint
     (files/topo-namespaces info-map))))
