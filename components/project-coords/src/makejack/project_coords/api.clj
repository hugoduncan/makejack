(ns makejack.project-coords.api
  "Manage the project data map, possibly in a project.edn file.

  The :name is a qualified symbol, like my.org/project-name.

  The :version is a dotted string, and can contain a computed component,
  specified as a :keyword.  Currently :git-rev-count and :reverse-date
  are supported."
  (:refer-clojure :exclude [read])
  (:require
   [makejack.project-coords.impl :as impl]))

(defn bump-version
  "Bump the project version.
  Return the new project coordinates."
  [options bump-type]
  (impl/bump-version options bump-type))

(defn expand-version
  "Expand the project version, replacing any keywords.

  Supported keywords are :git-rev-count and :reverse-date."
  [options]
  (impl/expand-version options))

(defn read
  "Read and return the project coordinates.
  Return map with :version and :version-map keys."
  [options]
  (impl/load-project options))

(defn write
  "Write the project coordinates to the project.edn file."
  {:arglists '[[{:keys [dir version version-map]}]]}
  [options]
  (impl/write-project options))
