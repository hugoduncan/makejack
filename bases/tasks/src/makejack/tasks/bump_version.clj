(ns makejack.tasks.bump-version
  (:require
   [makejack.defaults.api :as defaults]
   [makejack.project-data.api :as project-data]
   [makejack.verbose.api :as v]))

(defn bump-version
  "Bump the version of the project artifacts."
  [params]
  {:arglists '[[{:keys [level] :as params}]]}
  (v/println params "Bump version...")
  (let [data     (defaults/project-data params)
        new-data (project-data/bump-version data (:level params))]
    (project-data/write new-data)
    params))
