(ns makejack.tasks.update-dep
  (:require
   [makejack.deps-file.api :as deps-file]))

(defn ^{:params [:aritifact-name [:mvn/version][:git/sha][:git/tag]]}
  update-dep
  "Update a dependency in a deps.edn file.

  Options
    :dir             path to the deps.edn file
    :artifact-name   the name of the dependency to update
    :git/sha         new value for the git sha
    :git/tag         new value for the git tag
    :mvn/version     new value for the maven version

  Other tags in the coordinate map should work too."
  [params]
  (let [artifact-name (:artifact-name params)]
    (when-not artifact-name
      (throw (ex-info "update-dep expects a :artifact-name key value"
                      {:params params})))
    (deps-file/update-dep
     (update params :artifact-name symbol))))
