(ns makejack.project-coords.api
  (:refer-clojure :exclude [read])
  (:require
   [makejack.project-coords.impl :as impl]))

(defn bump-version
  "Bump the project version.
  Return the new project coordinates."
  [options bump-type]
  (impl/bump-version options bump-type))

(defn expand
  "Expand a version map, replacing any keywords."
  [options]
  (impl/expand options))

(defn read
  "Read and return the project coordinates.
  Return map with :version and :version-map keys."
  [options]
  (impl/load-project options))

(defn version->version-map
  "Convert a version string to a version map."
  [version]
  {:pre  [(string? version)]
   :post [map?]}
  (impl/version->version-map version))

(defn version-map->version
  "Convert a version map to a version string."
  [version-map]
  {:pre  [(map? version-map)]
   :post [string?]}
  (impl/version-map->version version-map))

(defn write
  "Write the project coordinates to the project.edn file."
  {:arglists '[[{:keys [dir version version-map]}]]}
  [options]
  (impl/write-project options))
