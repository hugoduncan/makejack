(ns makejack.jarfile.api
  (:require
   [makejack.jarfile.impl :as impl]))

(defn paths
  "Return a sequence of all the paths in the jar file."
  [jarfile-path]
  (impl/paths jarfile-path))
