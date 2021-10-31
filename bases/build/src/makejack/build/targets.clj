(ns makejack.build.targets
  "Build targets to build makejack itself.

  Uses a selection of makejack tasks."
  (:require
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


(defn poly-clj-kondo
  "Run clj-kondo over a polylith project.
  Assumes a .clj-kondo config at the polylith root.
  When the :init keyword is true, then intialise with all dependencies."
  [params]
  (tasks/poly-clj-kondo params))
