(ns makejack.defaults.api
  "Provide defaults for filesystem layouts, filenames, etc."
  (:require
   [makejack.defaults.impl :as impl]))

(defn target-path
  "Return the target directory."
  {:arglists '[[{:keys [target]}]]}
  [params]
  (impl/target-path params))

(defn classes-path
  "Return the classes directory."
  {:arglists '[[{:keys [target classes-dir]}]]}
  [params]
  (impl/classes-path params))

(defn project-coords
  "Return the project coordinates"
  {:arglists '[[{:keys [name version]}]]}
  [params]
  (impl/project-coords params))

(defn basis
  "Return the project basis
  :mvn/local dependencies are converted to use source paths."
  [params]
  (impl/basis params))

(defn paths
  "Return the basis :paths"
  [basis]
  (impl/paths basis))

(defn jar-filename
  "Return the jar filename."
  {:arglists '[[{:keys [name version]}]]}
  [params]
  {:pre [(:name params)(:version params)]}
  (impl/jar-filename params))

(defn jar-ignores
  "Adds .keep files to the default jar-ignores"
  []
  impl/jar-ignores)
