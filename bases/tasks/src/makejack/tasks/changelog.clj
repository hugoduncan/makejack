(ns makejack.tasks.changelog
  (:require
   [makejack.changelog.api :as changelog]
   [makejack.defaults.api :as defaults]
   [makejack.project-data.api :as project-data]
   [makejack.verbose.api :as v]))

(defn init [params]
  (let [version (->  params
                     defaults/project-data
                     project-data/expand-version
                     :version )]
    (changelog/changelog
     {:task    :init
      :version version})))

(defn release [params]
  (let [version (->  params
                     defaults/project-data
                     project-data/expand-version
                     :version )]
    (v/println params "Updating CHANGELOG/.md fpr release:" version)
    (changelog/changelog
     {:task    :release
      :version version})))
