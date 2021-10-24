(ns makejack.project-coords.api
  (:refer-clojure :exclude [load])
  (:require
   [makejack.project-coords.impl :as impl]))

(defn load
  "Load and return the project coordinates as a map."
  [options]
  (impl/load-project* options))

(defn bump-version
  "Bump the project version.
  Return the new project coordinates."
  [options]
  (impl/bump-version options))
