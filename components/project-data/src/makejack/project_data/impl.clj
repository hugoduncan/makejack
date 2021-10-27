(ns makejack.project-data.impl
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.filesystem.api :as fs]
   [makejack.path.api :as path]
   [rewrite-clj.zip :as z]))

;;; version <-> version-map

(defn- maybe-read [s]
  (try
    (edn/read-string s)
    (catch Exception _
      s)))

(defn version->version-map
  [version]
  {:pre [(string? version)]}
  (let [components (mapv maybe-read (str/split version #"[.]"))
        [non-q q]  (when (symbol? (last components))
                     (str/split (name (last components)) #"-"))
        components (cond-> components
                     q (conj (butlast components) (maybe-read non-q)))]
    (cond-> (zipmap [:major :minor :incremental] components)
      q (assoc :qualifier q))))

(defn version-map->version
  [version-map]
  {:pre  [(map? version-map)]
   :post [string?]}
  (let [components (keep version-map [:major :minor :incremental])
        q          (:qualifier version-map)]
    (cond-> (str/join "." components)
      q (str "-" q))))

;;; Version Expansion

(defmulti expand-component
  (fn [component] component))

(defmethod expand-component :default
  [component] component)

(defmethod expand-component :git-rev-count
  [_component] (edn/read-string (b/git-count-revs nil)))

(def reverse-date-fmt (java.text.SimpleDateFormat. "yyyy.MM.dd"))

(defmethod expand-component :reverse-date
  [_component] (.format reverse-date-fmt (java.util.Date.)))

(defn expand-version-map
  [version-map]
  (reduce-kv
   (fn [vm k value]
     (assoc vm k (expand-component value)))
   {}
   version-map))

(defn expand-version [params]
  (let [version (-> (:version params)
                    version->version-map
                    expand-version-map
                    version-map->version)]
    (assoc params :version version)))


;; (defn normalise
;;   [{:keys [version version-map] :as params}]
;;   (assert (or version version-map))
;;   (cond-> params
;;     (and version (not version-map))
;;     (assoc :version-map (version->version-map version))
;;     (and (not version) version-map)
;;     (assoc :version (version-map->version version-map))))

;;; File Handling

(defn project-edn-path
  ^java.nio.file.Path [{:keys [dir]}]
  (path/path (or dir ".") "project.edn"))

(def template-project-edn "{:name noname\n :version \"\"}")

(defn- validate-data!
  [{:keys [version name] :as data} f]
  (when-not version
    (throw (ex-info "project.edn must contain :version" {:path (str f)})))
  (when-not name
    (throw (ex-info "project.edn must contain :name" {:path (str f)})))
  data)

(defn load-project
  [params]
  (let [f (-> params project-edn-path path/as-file)]
    (-> f
        slurp
        edn/read-string
        (validate-data! f))))

(defn update-version
  [{:keys [name version]} src]
  (-> (z/of-string src)
      (z/subedit->
       (z/get :name)
       (z/replace name))
      (z/subedit->
       (z/get :version)
       (z/replace version))
      z/root-string))

(defn write-project
  [{:keys [name version] :as params}]
  {:pre [name version]}
  (let [f     (-> params project-edn-path path/as-file)
        src   (if (fs/file? f)
                (slurp f)
                template-project-edn)
        new-s (update-version params src)]
    (if new-s
      (spit f new-s)
      (throw (ex-info
              "Failed to write project.edn"
              {:path (str f)})))))

;;; bump

(defn bump-version [params bump-type]
  {:pre (#{:major :minor :incremental} bump-type)}
  (let [version (-> (:version params)
                    version->version-map
                    (update bump-type inc))
        to-zero (->> [:major :minor :incremental]
                     (drop-while #(not= bump-type %) )
                     next)
        version (reduce
                 (fn [version-map level]
                   (cond-> version-map
                     (let [v (version-map level)]
                       (and v (number? v)))
                     (assoc level 0)))
                 version
                 to-zero)]
    (assoc params :version (version-map->version version))))
