(ns makejack.project-coords.impl
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.filesystem.api :as fs]
   [makejack.path.api :as path]
   [rewrite-clj.zip :as z]))

;;; version <-> version-map

(defn version->version-map
  [version]
  {:pre [(string? version)]}
  (let [[non-q q]  (str/split version #"-")
        components (mapv edn/read-string (str/split non-q #"[.]"))]
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

;;; Expansion

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

(defn expand [params]
  (let [version-map (expand-version-map (:version-map params))
        version     (version-map->version version-map)]
    (assoc params
           :version version
           :version-map version-map)))


(defn normalise
  [{:keys [version version-map] :as params}]
  (assert (or version version-map))
  (cond-> params
    (and version (not version-map))
    (assoc :version-map (version->version-map version))
    (and (not version) version-map)
    (assoc :version (version-map->version version-map))))

;;; file handling

(defn project-edn-path
  ^java.nio.file.Path [{:keys [dir]}]
  (path/path (or dir ".") "project.edn"))

(def template-project-edn "{:name noname\n :version \"\"}")

(defn- validate-data!
  [{:keys [version version-map name]} f]
  (when (and version version-map)
    (throw
     (ex-info
      "project.edn must contain only one of :version and :version-map"
      {:path (str f)})))
  (when-not (or version version-map)
    (throw
     (ex-info
      "project.edn must contain either :version or :version-map"
      {:path (str f)})))
  (when-not name
    (throw (ex-info "project.edn must contain :name" {:path (str f)}))))

(defn load-project [params]
  (let [f (-> params project-edn-path path/as-file)
        {:keys [version version-map] :as data}
        (-> f slurp edn/read-string)]
    (validate-data! data f)
    (cond-> data
      (not version)
      (assoc :version (version-map->version version-map))

      (not version-map)
      (assoc :version-map (version->version-map version)))))

(defn update-version
  [{:keys [name version]} src]
  (-> (z/of-string src)
      (z/subedit->
       (z/get :name)
       (z/replace name))
      (z/subedit->
       (z/get :version)
       (z/replace version))))

(defn update-version-map
  [{:keys                                       [name]
    {:keys [major minor incremental qualifier]} :version-map} src]
  (-> (z/of-string src)
      (z/subedit->
       (z/get :name)
       (z/replace name))
      (z/subedit->
       (z/get :version-map)
       (z/subedit->
        (z/get :major)
        (z/replace (str major)))
       (cond->
           minor (z/subedit->
                  (some->
                   (z/get :minor)
                   (z/replace (str minor)))))
       (cond->
           incremental (z/subedit->
                        (some->
                         (z/get :incremental)
                         (z/replace (str incremental)))))
       (cond->
           qualifier (z/subedit->
                      (some->
                       (z/get :qualifier)
                       (z/replace (str qualifier))))))))

(defn write-project [{:keys [name] :as params}]
  (let [{:keys [version version-map] :as params}
        (normalise params)

        _       (prn :params params)
        f       (-> params project-edn-path path/as-file)
        src     (if (fs/file? f)
                  (slurp f)
                  template-project-edn)
        current (edn/read-string src)
        zloc    (if (:version current)
                  (update-version params src)
                  (update-version-map params src))
        new-s   (z/root-string zloc)]
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
