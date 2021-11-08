(ns makejack.poly.impl
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [polylith.clj.core.api.interface :as poly-api]))

(defn workspace [{:keys [base keys] :or {base :stable keys nil}}]
  (poly-api/workspace base keys))

(defn element-paths
  [prefix elements]
  (mapv (fn [p] (str (fs/path (name prefix) (:name p)))) elements))

(defn elements
  [ws]
  (fn [e]
    (some->> (ws e)
             (element-paths e))))

(defn all-elements [ws]
  (let [ws (or ws {})
        f  (elements ws)]
    (reduce
     into
     []
     (mapv f [:components :bases :projects]))))

(defn changed-element-paths
  [prefix elements]
  (mapv (fn [p] (str (fs/path (name prefix)  p))) elements))

(defn changed-element
  [ws]
  (fn [e]
    (some->> (ws (keyword (str "changed-" (name e))))
             (changed-element-paths e))))

(defn changed-elements [ws elements]
  (let [ws (or ws {})
        f  (changed-element (:changes ws {}))]
    (reduce into [] (mapv f elements))))

(defn resolve-project
  [ws spec]
  (prn :spec spec)
  (cond
    (nil? spec)
    (prn :changed-projects)
    (= 'all spec)
    (prn :all-projects)
    :else
    (prn :project-name spec)))

(defn resolve-elements
  "Given a polylith spec string, return paths to the resolved elements.

  "
  [ws spec]
  (let [ws    (or ws {})
        specs (mapv symbol (remove str/blank? (str/split (str spec) #":")))]
    (prn :specs specs)
    (cond
      (= 'project (first specs))
      (resolve-project ws (second specs))
      (= 'brick (first specs))
      (prn :brick specs)
      (= 'base (first specs))
      (prn :base specs)
      (= 'component (first specs))
      (prn :component specs))))
