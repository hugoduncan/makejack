(ns makejack.project-data.api
  "Manage the project data map, possibly in a project.edn file.

  The :name is a qualified symbol, like my.org/project-name.

  The :version is a dotted string, and can contain a computed component,
  specified as a :keyword.  Currently :git-rev-count and :reverse-date
  are supported."
  (:refer-clojure :exclude [read])
  (:require
   [makejack.project-data.impl :as impl]))

(defn bump-version
  "Bump the given level of the project version.

  bump-tupe is one of :major, :minor and :incremental

  Return the new project data."
  [options bump-type]
  (impl/bump-version options bump-type))

(defn expand-version
  "Expand the project version, replacing any keywords.

  Supported keywords are :git-rev-count and :reverse-date."
  [options]
  (impl/expand-version options))

(defn read
  "Read and return the project data.
  Return a map with the edn data read from project.edn."
  [options]
  (impl/load-project options))

(defn write
  "Write the project data to the project.edn file.
  Perform a whitespace and comment preserving update of an existing file."
  {:arglists '[[{:keys [dir version version-map]}]]}
  [options]
  (impl/write-project options))

(defn write-version-file
  "Write the project version to :path, defaulting to \"version.edn\""
  [params]
  (impl/write-version-file params))

(defn read-version-file
  "Read the project version from :path, defaulting to \"version.edn\""
  [params]
  (impl/read-version-file params))
