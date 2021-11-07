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
   [babashka.fs :as fs]
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.files.api :as files]
   [makejack.git.api :as git]
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

(defn install
  "install jar to local maven repository."
  [params]
  (v/println params "Install jar...")
  (let [params    (defaults/project-data params)
        params    (project-data/expand-version params)
        jar-path  (fs/path
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


(defn clj-kondo
  "lint with clj-kondo."
  [{:keys [cache-dir config-dir dir] :or {dir "."} :as params}]
  (v/println params "clj-kondo" dir "...")
  (let [basis        (defaults/basis params)
        paths        (if (:init params)
                       (keys (:classpath basis))
                       (:paths basis))
        command-args (cond-> ["clj-kondo" "--lint" (str/join ":" paths)]
                       (:init params)
                       (into ["--dependencies" "--parallel" "--copy-configs"])
                       config-dir
                       (into ["--config-dir" config-dir])
                       cache-dir
                       (into ["--cache-dir" cache-dir]))]
    (->
     {:command-args command-args
      :dir          (.getPath (b/resolve-path dir))
      :out          :inherit}
     b/process)
    params))


(defn ns-tree
  "Return namespace tree info."
  [params]
  (let [basis    (defaults/basis params)
        info-map (files/info-map params (defaults/paths basis))]
    (println "Unreferenced namespaces"
             (files/top-level-nses info-map))
    (clojure.pprint/pprint
     (files/topo-namespaces info-map))))

(defn compile-clj
  "AOT complilation"
  [params]
  (v/println params "compile-clj...")
  (let [basis     (defaults/basis params)
        class-dir (defaults/classes-path params)
        info-map  (files/info-map params (defaults/paths basis))
        _         (assert (some? info-map))
        nses      (files/topo-namespaces info-map)]
    (binding [b/*project-root* (:dir params ".")]
      (b/compile-clj
       {:class-dir  class-dir
        :basis      basis
        :ns-compile (remove #(re-matches #"hooks.*" (str %)) nses) }))
    (assoc params :class-dir class-dir)))

(defn javac
  "Java complilation"
  [params]
  (v/println params "compile-java...")
  (let [basis (defaults/basis params)]
    (b/javac
     {:class-dir  (defaults/classes-path params)
      :basis      basis
      :src-dirs   (:java-paths basis)
      :javac-opts (:javac-opts params)})))
