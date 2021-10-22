(ns makejack.project-coords.api
  (:refer-clojure :exclude [load])
  (:require
   [makejack.project-coords.impl :as impl]))

(defn load [options]
  "Load and return the project coordinates as a map."
  (impl/load-project* options))

(defn bump-version [options]
  "Bump the project version.
     Return the new project coordinates."
  (impl/bump-version options))
