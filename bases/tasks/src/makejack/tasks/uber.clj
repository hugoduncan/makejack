(ns makejack.tasks.uber
  (:require
   [babashka.fs :as fs]
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.project-data.api :as project-data]
   [makejack.verbose.api :as v]))

(defn uber
  "Build an uberjar file"
  [params]
  (v/println params "Build uberjar...")
  (binding [b/*project-root* (:dir params ".")]
    (let [params    (defaults/project-data params)
          params    (project-data/expand-version params)
          jar-path  (fs/path
                     (defaults/target-path params)
                     (defaults/jar-filename params))
          basis     (or (:basis params) (defaults/basis params))
          src-dirs  (defaults/paths basis)
          class-dir (str (defaults/classes-path params))
          relative? (complement fs/absolute?)]
      (b/write-pom {:basis     (update basis :paths
                                       #(filterv relative? %))
                    :class-dir class-dir
                    :lib       (:name params)
                    :version   (:version params)})
      (b/copy-dir {:src-dirs   src-dirs
                   :target-dir class-dir
                   :ignores    (defaults/jar-ignores)})
      (b/uber (merge
               (select-keys
                params
                [:conflict-handlers :exclude :manifest :main])
               {:class-dir class-dir
                :uber-file (str jar-path)}))
      (assoc params :jar-file (str jar-path)))))
