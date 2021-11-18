(ns makejack.defaults.impl
  (:require
   [babashka.fs :as fs]
   [clojure.tools.build.api :as b]
   [makejack.project-data.api :as project-data]))

(defn target-path
  "Return the target directory."
  [params]
  (:target params (str (fs/path (:dir params ".") "target"))))

(defn classes-path
  "Return the classes directory."
  [params]
  (str (fs/path (target-path params) (:classes-dir params "classes"))))


(defn project-data
  "Return the params with project coordinates"
  [{:keys [name version] :as params}]
  (assert (or (and name version) (and (not name) (not version)))
          "Both :name and :version must be specified, or both unspecified.")
  (if name
    params
    (merge params (project-data/read params))))

(defn basis
  "Return the project basis
  :mvn/local dependencies are converted to use source paths."
  [params]
  (let [aliases (:aliases params)]
    (binding [b/*project-root* (:dir params ".")]
      (-> (b/create-basis
           (select-keys
            params
            [:aliases :project :root :user :extra]))))))

(defn paths
  "Return the basis :paths"
  [basis]
  (:paths basis))

(defn jar-filename
  "Return the jar filename."
  [params]
  {:pre [(:name params)(:version params)]}
  (format "%s-%s.jar" (name (:name params)) (:version params)))

(def jar-ignores
  "Adds .keep files to the default jar-ignores"
  [".*~$" "^#.*#$" "^\\.#.*" "^.DS_Store$" "^.keep$"])

(defn git-tag-for-version
  [params]
  (str "v" (:version params)))
