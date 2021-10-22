(ns hooks.fs
  (:require
   [clj-kondo.hooks-api :as api]))

(defn with-binding
  "A form that has a first vector argument and a body.
  The first element of the vector is considered a binding.  Other
  elements are evaluated."
  [{:keys [node]}]
  (let [[_ binding-vec & body] (:children node)
        [binding & others]     (:children binding-vec)
        new-node               (api/list-node
                                (list*
                                 (api/token-node 'let)
                                 (api/vector-node
                                  (reduce
                                   into
                                   [binding (api/token-node 'nil)]
                                   (mapv
                                    #(vector (api/token-node '_) %)
                                    others)))
                                 body))]
    ;; un-comment below to debug changes
    #_(prn :with-binding (api/sexpr new-node))
    {:node (with-meta new-node (meta node))}))
