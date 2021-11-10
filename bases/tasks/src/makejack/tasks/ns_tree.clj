(ns makejack.tasks.ns-tree
  (:require
   [clojure.pprint :as pprint]
   [makejack.defaults.api :as defaults]
   [makejack.files.api :as files]))

(defn ns-tree
  "Return namespace tree info."
  [params]
  (let [basis    (defaults/basis params)
        info-map (files/info-map params (defaults/paths basis))]
    (println "Unreferenced namespaces"
             (files/top-level-nses info-map))
    (pprint/pprint
     (files/topo-namespaces info-map))))
