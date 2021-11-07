(ns makejack.poly.impl
  (:require
   [clojure.string :as str]
   [makejack.path.api :as path]
   [polylith.clj.core.api.interface :as poly-api]))

(defn lift-local-deps
  "Return a basis with :mvn/local deps converted to source dependencies.
  Adds transitive libs, and extends the paths."
  [basis]
  ;; NOTE this could be simpler if we constructed a new deps map and used
  ;; b/create-basis, but that complains about non-local source paths.
  (let [libs            (:libs basis)
        local-root      #(:local/root (get libs %))
        transitive-deps (reduce-kv
                         (fn [deps lib {:keys [dependents] :as coords}]
                           (let [version (:mvn/version coords)]
                             (if (and version (some local-root dependents))
                               (assoc deps lib {:mvn/version version})
                               deps)))
                         {}
                         libs)
        local-paths     (reduce-kv
                         (fn [ps _lib {:keys [dependents paths] :as coords}]
                           (if (and (:local/root coords)
                                    (or (empty? dependents)
                                        (some local-root dependents)))
                             (into ps paths)
                             ps))
                         []
                         libs)
        transitive-libs (reduce-kv
                         (fn [deps lib coords]
                           (if (transitive-deps lib)
                             (assoc deps lib (assoc coords :dependents []))
                             deps))
                         {}
                         libs)]
    ;; (prn :poly (get libs 'polylith/clj-api))
    ;; (prn :poly-util (get libs 'poly/util))
    ;; (prn :makejack-path (get libs 'makejack/path))
    ;; (prn :libs (keys libs))
    ;; (prn :transitive-deps transitive-deps)
    ;; (prn :local-paths local-paths)
    (-> basis
        (update :libs #(into {} (remove (comp :local/root val)) %))
        (update :libs merge transitive-libs)
        (update :deps merge transitive-deps)
        (update :deps #(into {} (remove (comp :local/root val)) %))
        (update :paths into local-paths))))

(defn workspace [{:keys [base keys] :or {base :stable keys nil}}]
  (poly-api/workspace base keys))

(defn element-paths
  [prefix elements]
  (mapv (fn [p] (str (path/path (name prefix) (:name p)))) elements))

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
  (mapv (fn [p] (str (path/path (name prefix)  p))) elements))

(defn changed-element
  [ws]
  (fn [e]
    (some->> (ws (keyword (str "changed-" (name e))))
             (changed-element-paths e))))

(defn changed-elements [ws]
  (let [ws (or ws {})
        f  (changed-element (:changes ws {}))]
    (reduce
     into
     []
     (mapv f [:components :bases :projects]))))


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
