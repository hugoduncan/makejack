(ns makejack.tasks.project-data
  (:require
   [makejack.defaults.api :as defaults]
   [makejack.project-data.api :as project-data]))

(defn project-data
  "Read and expand project.edn"
  [params]
  (-> params
      defaults/project-data
      project-data/expand-version))
