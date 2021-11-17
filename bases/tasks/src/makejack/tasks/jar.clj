(ns makejack.tasks.jar
  (:require
   [babashka.fs :as fs]
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.deps.api :as mj-deps]
   [makejack.project-data.api :as project-data]
   [makejack.verbose.api :as v]))

(defn jar
  "Build a jar file"
  [params]
  (binding [b/*project-root* (:dir params ".")]
    (let [params    (defaults/project-data params)
          params    (project-data/expand-version params)
          jar-path  (fs/path
                     (defaults/target-path params)
                     (defaults/jar-filename params))
          basis     (mj-deps/lift-local-deps
                     (or (:basis params) (defaults/basis params)))
          src-dirs  (defaults/paths basis)
          class-dir (str (defaults/classes-path params))
          relative? (complement fs/absolute?)]
      (v/println params "Build jar" (str jar-path))
      (binding [b/*project-root* (:dir params ".")]
        (b/write-pom {:basis     (update basis :paths
                                         #(filterv relative? %))
                      :class-dir class-dir
                      :lib       (:name params)
                      :version   (:version params)})
        (b/copy-dir {:src-dirs   src-dirs
                     :target-dir class-dir
                     :ignores    (defaults/jar-ignores)})
        (b/jar {:class-dir class-dir
                :jar-file  (str jar-path)}))
      (assoc params :jar-file (str jar-path)))))
