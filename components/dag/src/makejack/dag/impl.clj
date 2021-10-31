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
  {:pre [(not= from to)]}
  (assert (not ((transitive (:child-edges graph) from) to)))
  (->Graph
   (update (:parent-edges graph) to set-conj from)
   (update (:child-edges graph) from set-conj to)))
