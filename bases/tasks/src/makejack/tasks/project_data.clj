(ns makejack.tasks.project-data
  (:require
   [makejack.defaults.api :as defaults]
   [makejack.project-data.api :as project-data]
   [makejack.verbose.api :as v]))

(defn project-data
  "Read and expand project.edn"
  [params]
  (v/println "Reading project data")
  (-> params
      defaults/project-data
      project-data/expand-version))

(defn write-version
  "Write the current project version to project.edn."
  [params]
  (v/println "Writing version to project data")
  (project-data/write params))

(defn write-version-file
  "Write the current project version to a file with an edn map.
  By default writes to version.edn."
  [params]
  (v/println "Writing version file")
  (project-data/write-version-file params))

(defn read-version-file
  "Read the project version from a file with an edn map.
  By default writes to version.edn."
  [params]
  (v/println "Reading version file")
  (project-data/read-version-file params))
