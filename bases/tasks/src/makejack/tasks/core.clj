(ns makejack.tasks.core
  "Tasks and utilities for tools.build.

  Behaviour is controlled via a params map, that is typically passed from
  the clojure CLI invocation via a -T argument.

  The params have the following defaults:

  :target  \"target\"
  :verbose true

  The :lib and :version keys are populated from `project.edn` if
  present, else must be manually supplied."
  (:require
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.git.api :as git]
   [makejack.path.api :as path]
   [makejack.project-data.api :as project-data]
   [makejack.target-doc.api :as target-doc]
   [makejack.verbose.api :as v]))

(defn ^{:doc-order 0 :params "[:target target-name]"} help
  "Show help

  Use :target to get detailed help on a specific target."
  ([params] (help params 'makejack.tasks.core))
  ([params ns-ref]
   ;; TODO: make this a function in target-doc
   (println)
   (println (target-doc/help-task params ns-ref))
   params))

(defn clean
  "Remove the target directory."
  {:arglists '[[{:keys [target] :as params}]]}
  [params]
  (v/println params "Clean target...")
  (b/delete {:path (defaults/target-path params)})
  params)

(defn bump-version
  "Bump the version of the project artifacts."
  [params]
  {:arglists '[[{:keys [level] :as params}]]}
  (v/println params "Bump version...")
  (let [data     (defaults/project-data params)
        new-data (project-data/bump-version data (:level params))]
    (project-data/write new-data)
    params))

(defn jar
  "Build a jar file"
  [params]
  (v/println params "Build jar...")
  (let [params    (defaults/project-data params)
        params    (project-data/expand-version params)
        jar-path  (path/path
                   (defaults/target-path params)
                   (defaults/jar-filename params))
        basis     (or (:basis params) (defaults/basis params))
        src-dirs  (defaults/paths basis)
        class-dir (str (defaults/classes-path params))
        relative? (complement path/absolute?)]
    (b/write-pom {:basis     (update basis :paths
                                     #(filterv relative? %))
                  :class-dir class-dir
                  :lib       (:name params)
                  :version   (:version params)})
    (b/copy-dir {:src-dirs   src-dirs
                 :target-dir class-dir
                 :ignores    (defaults/jar-ignores)})
    (b/jar {:class-dir class-dir
            :jar-file  (str jar-path)})
    params))

(defn uber
  "Build an uberjar file"
  [params]
  (v/println params "Build uberjar...")
  (let [params    (defaults/project-data params)
        params    (project-data/expand-version params)
        jar-path  (path/path
                   (defaults/target-path params)
                   (defaults/jar-filename params))
        basis     (or (:basis params) (defaults/basis params))
        src-dirs  (defaults/paths basis)
        class-dir (str (defaults/classes-path params))
        relative? (complement path/absolute?)]
    (b/write-pom {:basis     (update basis :paths
                                     #(filterv relative? %))
                  :class-dir class-dir
                  :lib       (:name params)
                  :version   (:version params)})
    (b/copy-dir {:src-dirs   src-dirs
                 :target-dir class-dir
                 :ignores    (defaults/jar-ignores)})
    (b/uber {:class-dir class-dir
             :uber-file (str jar-path)})
    params))

(defn install
  "install jar to local maven repository."
  [params]
  (v/println params "Install jar...")
  (let [params    (defaults/project-data params)
        params    (project-data/expand-version params)
        jar-path  (path/path
                   (defaults/target-path params)
                   (defaults/jar-filename params))
        basis     (or (:basis params) (defaults/basis params))
        class-dir (str (defaults/classes-path params))]
    (b/install
     {:basis     basis
      :class-dir class-dir
      :jar-file  (str jar-path)
      :lib       (:name params)
      :version   (:version params)})
    params))

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
