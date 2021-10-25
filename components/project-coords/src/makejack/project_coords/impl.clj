(ns makejack.project-coords.impl
  (:require
   [clojure.edn :as edn]
   [makejack.path.api :as path]))

(defn load-project* [& [{:keys [dir] :as options}]]
  (edn/read-string
   (slurp (path/as-file (path/path dir "project.edn")))))


(defn bump-version [option s])
