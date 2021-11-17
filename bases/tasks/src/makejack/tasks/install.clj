(ns makejack.tasks.install
  (:require
   [babashka.fs :as fs]
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.project-data.api :as project-data]
   [makejack.verbose.api :as v]))

(defn install
  "install jar to local maven repository."
  [params]
  (let [params    (defaults/project-data params)
        params    (project-data/expand-version params)
        jar-path  (fs/path
                   (defaults/target-path params)
                   (defaults/jar-filename params))
        basis     (or (:basis params) (defaults/basis params))
        class-dir (str (defaults/classes-path params))
        proj-name (:name params)
        version   (:version params)]
    (v/println params "Install jar" (str jar-path) "for" proj-name version)
    (b/install
     {:basis     basis
      :class-dir class-dir
      :jar-file  (str jar-path)
      :lib       proj-name
      :version   version})
    params))
