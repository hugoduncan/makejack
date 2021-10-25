(ns makejack.defaults.impl
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.filesystem.api :as fs]
   [makejack.path.api :as path]
   [makejack.poly.api :as poly]
   [makejack.project-coords.api :as project-coords]))

(defn target-path
  "Return the target directory."
  [params]
  (:target params "target"))

(defn classes-path
  "Return the classes directory."
  [params]
  (str (path/path (target-path params) (:classes-dir params "classes"))))

(defn load-project-coords
  "Return the project coordinates"
  []
  (let [coords    (project-coords/load {:dir "."})
        lib       (symbol (format "%s/%s" (:group-id coords) (:name coords)))
        rev-count (b/git-count-revs nil)
        version   (format
                   "%s.%s"
                   (:version coords)
                   (if (str/blank? rev-count) "0" rev-count))]
    {:lib     lib
     :version version}))

(defn project-coords
  "Return the project coordinates"
  [{:keys [lib version]}]
  (assert (or (and lib version) (and (not lib) (not version)))
          "Both :lib and :version must be specified, or both unspecified.")
  (if lib
    {:lib lib :version version}
    (load-project-coords)))

(defn basis
  "Return the project basis
  :mvn/local dependencies are converted to use source paths."
  [params]
  (-> (b/create-basis (select-keys params [:project]))
      poly/lift-local-deps))

(defn paths
  "Return the basis :paths"
  [basis]
  (:paths basis))

(defn jar-filename
  "Return the jar filename."
  [params]
  {:pre [(:lib params)(:version params)]}
  (format "%s-%s.jar" (name (:lib params)) (:version params)))

(def jar-ignores
  "Adds .keep files to the default jar-ignores"
  [".*~$" "^#.*#$" "^\\.#.*" "^.DS_Store$" "^.keep$"])
