(ns makejack.build.targets
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.poly.api :as poly]
   [makejack.project-coords.api :as project-coords]
   [makejack.target-doc.api :as target-doc]
   [makejack.tasks.core :as tasks]))

;; (def class-dir "target/classes")


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

(defn jar
  "Build a jar file"
  [params]
  (tasks/jar params))

(defn install
  "install jar to local maven repository."
  [params]
  (tasks/install params))
