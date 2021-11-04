(ns makejack.dag.api
  (:refer-clojure :exclude [parents])
  (:require
   [makejack.dag.impl :as impl]))

(defn graph []
  (impl/graph))

(defn children
  [graph node]
  (impl/children graph node))

(defn parents
  [graph node]
  (impl/parents graph node))

(defn transitive-children
  [graph node]
  (impl/transitive-children graph node))

(defn transitive-parents
  [graph node]
  (impl/transitive-parents graph node))

(defn add-edge
  [graph from to]
  {:pre [(not= from to)]}

  (impl/add-edge graph from to))


(defn topo-sort
  [child-edges parent-edges ks]
  (impl/topo-sort child-edges parent-edges ks))
