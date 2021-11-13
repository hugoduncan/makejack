(ns makejack.tasks.impl)

(defn add-ns-default [defaults]
  (merge {:ns (list 'quote (ns-name *ns*))} defaults))

(defn normalise-spec [spec]
  (let [m (if (symbol? spec)
            {:sym spec :as spec :defaults nil}
            (merge {:as (first spec)}
                   {:sym (first spec)}
                   (apply hash-map (rest spec))))]
    (cond-> m
      (= 'help (:sym m))
      (update :defaults add-ns-default))))

(defn wrap-one [spec]
  (let [{:keys [sym as defaults]} (normalise-spec spec)
        params-sym                (gensym "params")
        target-sym                (symbol "makejack.tasks" (name sym))
        v                         (resolve target-sym)
        as                        (vary-meta
                                   as merge
                                   (dissoc (meta v)
                                           :ns :name :file :column :line
                                           :arglists))]
    `(defn ~as [~params-sym]
       (~target-sym
        ~(if defaults
           `(merge ~defaults ~params-sym)
           params-sym)))))
