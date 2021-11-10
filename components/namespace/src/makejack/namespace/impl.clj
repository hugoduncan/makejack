(ns makejack.namespace.impl
  (:refer-clojure :exclude [ns-imports])
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]
   [clojure.tools.reader :as reader])
  (:import
   [java.io PushbackReader]))

(defn namespace-declaration?
  "Predicate for a (ns ...) declaration form."
  [form]
  (and (list? form) (= 'ns (first form))))

(defn read-namespace-declaration
  "Read a namespace declaration.
  Return the namespace declaration form, or nil if not found."
  [io-reader tools-reader-opts]
  (loop []
    (let [options (assoc tools-reader-opts :eof ::eof)
          form    (reader/read options io-reader)]
      (cond
        (= ::eof form)                nil
        (namespace-declaration? form) form
        :else                         (recur)))))

(defn read-features
  "tools.reader/read options to read conditionals with the :clj feature."
  [features]
  {:read-cond :allow
   :features  (set features)})

(defn ns-form
  ([path] (ns-form path #{:clj}))
  ([path features]
   (with-open [reader (PushbackReader.
                       (io/reader
                        (fs/file (fs/path path))))]
     (read-namespace-declaration reader (read-features features)))))

(defn declared-ns [form]
  (second form))

(defn join-import [prefix]
  (let [p (str prefix ".")]
    (fn [s]
      (symbol (str p s)))))

(defn ns-imports [imports]
  (reduce
   (fn [res i]
     (cond
       (symbol? i)     (conj res i)
       (sequential? i) (into res (map (join-import (first i)) (rest i)))
       :else           res))
   #{}
   imports))

(defn join-ns [prefix]
  (let [p (str prefix ".")]
    (fn [m]
      (assert map? m)
      (update m :namespace #(symbol (str p %))))))

(defn normalise-require [req]
  (cond
    (symbol? req)
    {:namespace req}

    (and (sequential? req) (keyword? (second req)))
    (assoc (apply hash-map (rest req))
           :namespace (first req))

    (sequential? req)
    (mapv (join-ns (first req)) (mapv normalise-require (rest req)))))

(defn ns-requires [requires]
  (reduce
   (fn [res i]
     (let [r (normalise-require i)]
       (if (map? r)
         (conj res r)
         (into res r))))
   #{}
   requires))

(defmulti clause-info
  "Return info map for a ns clause"
  first)

(defmethod clause-info :default
  [_]
  nil)

(defmethod clause-info :import
  [clause]
  {:import (ns-imports (rest clause))})

(defmethod clause-info :require
  [clause]
  {:require (ns-requires (rest clause))})

(defn parse
  [form]
  (reduce
   (fn [res clause]
     (merge res (clause-info clause)))
   {:namespace (declared-ns form)}
   (drop 2 form)))

(defn dependencies
  ([path] (dependencies path #{:clj}))
  ([path features]
   (-> path
       (ns-form features)
       parse)))
