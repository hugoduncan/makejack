(ns makejack.dag.impl
  (:refer-clojure :exclude [parents]))

(defn transitive
  [edges node]
  (loop [new-nodes (edges node)
         result    #{}]
    (if-let [[node & nodes] (seq new-nodes)]
      (if (contains? result node)
        (recur nodes result)
        (recur (concat nodes (edges node))
               (conj result node)))
      result)))

(defrecord Graph
    [parent-edges child-edges])

(defn graph [] (->Graph {} {}))

(defn children
  [graph node]
  ((:child-edges graph) node))

(defn parents
  [graph node]
  ((:parent-edges graph) node))

(defn transitive-children
  [graph node]
  (transitive (:child-edges graph) node))

(defn transitive-parents
  [graph node]
  (transitive (:parent-edges graph) node))

(def set-conj (fnil conj #{}))

(defn add-edge
  [graph from to]
  {:pre [(not= from to)
         (some? from)
         (some? to)]}
  (assert (not ((transitive (:child-edges graph {}) to) from))
          (str {:from from :to to}))
  (->Graph
   (update (:parent-edges graph) to set-conj from)
   (update (:child-edges graph) from set-conj to)))


;; from clojure 1.11-aplha-2
(defn update-vals
  "m f => {k (f v) ...}

  Given a map m and a function f of 1-argument, returns a new map where
  the keys of m are mapped to result of applying f to the corresponding
  values of m."
  [m f]
  (with-meta
    (persistent!
     (reduce-kv (fn [acc k v] (assoc! acc k (f v)))
                (if (instance? clojure.lang.IEditableCollection m)
                  (transient m)
                  (transient {}))
                m))
    (meta m)))


;; Kahn, A. B. ‘Topological Sorting of Large Networks’.
;; Communications of the ACM 5, no. 11 (November 1962): 558–62.
;; https://doi.org/10.1145/368996.369025.

(defn topo-sort
  "Topological sort.
  Uses Kahns algorith."
  [child-edges parent-edges ks]
  (assert (map? child-edges))
  (assert (map? parent-edges))
  (let [zero-k (fn [k]
                 (when (zero? (count (parent-edges k)))
                   k))
        q      (reduce
                conj
                clojure.lang.PersistentQueue/EMPTY
                (keep zero-k ks))
        cnts   (update-vals parent-edges count)]
    #_(prn :edges child-edges :cnts cnts)
    (loop [q    q
           cnts cnts
           ans  []]
      #_(prn :q q)
      (let [h       (first q)
            ans     (conj ans h)
            q       (pop q)
            ns      (child-edges h {})
            ns-cnts (update-vals (select-keys cnts ns) dec)
            to-q    (keep #(when (zero? (ns-cnts % 0)) %) ns)
            q       (into q to-q)]
        #_(prn :q q :cnts cnts :ns ns :ns-cnts ns-cnts :to-q to-q :ans ans)

        (if (not-empty q)
          (recur q (merge cnts ns-cnts) ans)
          ans)))))
