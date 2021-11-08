(ns makejack.deps.impl)


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
    (-> basis
        (update :libs #(into {} (remove (comp :local/root val)) %))
        (update :libs merge transitive-libs)
        (update :deps merge transitive-deps)
        (update :deps #(into {} (remove (comp :local/root val)) %))
        (update :paths into local-paths))))
