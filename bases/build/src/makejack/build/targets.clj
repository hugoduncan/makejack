(ns makejack.build.targets
  "Build targets to build makejack itself.

  Uses a selection of makejack tasks."
  (:require
   [clojure.string :as str]
   [makejack.tasks.core :as tasks]))

(defn ^{:doc-order 0 :params "[:target target-name]"} help
  "Show help

  Use :target to get detailed help on a specific target."
  [params]
  ;; TODO: make this a function in target-doc
  (tasks/help params 'makejack.build.targets))

(defn clean
  "Remove all built files"
  [params]
  (tasks/clean params))

(defn bump-version
  "Bump the project version at a given level."
  [params]
  (tasks/bump-version params))

(defn jar
  "Build a jar file"
  [params]
  (tasks/jar params))

(defn uber
  "Build a uberjar file"
  [params]
  (tasks/uber params))

(defn install
  "install jar to local maven repository."
  [params]
  (tasks/install params))

(defn tag-version
  "Add a git tag with the latest version tag."
  [params]
  (tasks/tag-version params))

(defn clj-kondo
  "Run clj-kondo.
  When the :init keyword is true, then intialise with all dependencies."
  [params]
  (tasks/clj-kondo params))
(defn ns-tree
  "Return namespace tree info."
  [params]
  (tasks/ns-tree params))

(defn- aliases-spec-to-aliases
  [spec]
  (cond
    (keyword? spec) [spec]
    (string? spec)  (mapv keyword (str/split spec #"\."))
    :else           spec))

(defn normalise-aliases [params]
  (cond-> params
    (:aliases params)
    (update :aliases aliases-spec-to-aliases)))

(defn compile-clj
  "AOT compile clojure namespaces"
  [params]
  (tasks/compile-clj (normalise-aliases params)))

(defn javac
  "javaac compile java classes"
  [params]
  (tasks/javac (normalise-aliases params)))

(defn poly
  "Run a mj task on a polylith project.

  Unrecognised options are passed to the target.

  Options:
    - :on     as per `poly test`, e.g :project
    - :target  the mj target to invoke."
  [params]
  ((requiring-resolve 'makejack.tasks.poly/poly)
   (normalise-aliases params)))

(defn poly-clj-kondo
  "Run clj-kondo over a polylith project.
  Assumes a .clj-kondo config at the polylith root.
  When the :init keyword is true, then intialise with all dependencies."
  [params]
  ((requiring-resolve 'makejack.tasks.poly/poly-clj-kondo)
   params))

(defn poly-main
  "clojure -m in namespaces"
  [params]
  ((requiring-resolve 'makejack.tasks.poly/poly-main)
   (normalise-aliases params)))
