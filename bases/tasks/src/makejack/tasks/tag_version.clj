(ns makejack.tasks.tag-version
  (:require
   [makejack.defaults.api :as defaults]
   [makejack.git.api :as git]
   [makejack.project-data.api :as project-data]
   [makejack.verbose.api :as v]))

(defn tag-version
  "Tag the HEAD sha with the project version."
  [params]
  (v/println params "Tag version...")
  (let [params (defaults/project-data params)
        params (project-data/expand-version params)
        tag    (defaults/git-tag-for-version params)]
    (git/tag
     (merge
      (select-keys params [:dir])
      {:tag tag}))
    params))
