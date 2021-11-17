(ns makejack.deps-file.api
  (:require
   [makejack.deps-file.impl :as impl]))

(defn update-dep
  "Update a dependency in the deps.edn file.

  Options:
    :dir            directory containing the deps.edn file
    :artifact-name  name of dependency to change
    :mvn/version    new maven version
    :git/sha        new maven version"
  [params]
  (impl/update-dep params))
