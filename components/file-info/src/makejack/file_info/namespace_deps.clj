(ns makejack.file-info.namespace-deps
  (:refer-clojure :exclude [ns-imports])
  (:require
   [clojure.java.io :as io]
   [clojure.tools.reader :as reader]
   [makejack.path.api :as path])
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
                        (path/as-file (path/as-path path))))]
     (read-namespace-declaration reader (read-features features)))))


(defn declared-ns [form]
  (second form))

(defn join-ns [prefix]
  (let [p (str prefix ".")]
    (fn [s]
      (symbol (str p s)))))

(defn ns-imports [imports]
  (reduce
   (fn [res i]
     (cond
       (symbol? i)     (conj res i)
       (sequential? i) (into res (map (join-ns (first i)) (rest i)))
       :else           res))
   []
   imports))

(defn normalise-require [req]
  (cond
    (symbol? req)                 req
    (and (sequential? req)
         (keyword? (second req))) (first req)
    (sequential? req)             (mapv
                                   (join-ns (first req))
                                   (mapv normalise-require (rest req)))))

(defn ns-requires [requires]
  (reduce
   (fn [res i]
     (let [r (normalise-require i)]
       (if (symbol? r)
         (conj res r)
         (into res r))))
   []
   requires))

(defmulti clause-info
  "Return info map for a ns clause"
  first)

(defmethod clause-info :default
  [_]
  nil)

(defmethod clause-info :import
  [clause]
  {:imports (set (ns-imports (rest clause)))})

(defmethod clause-info :require
  [clause]
  {:requires (set (ns-requires (rest clause)))})

(defn parse-form [form]
  (reduce
   (fn [res clause]
     (merge res (clause-info clause)))
   {:namespace (declared-ns form)}
   (drop 2 form)))

(defn dependencies
  ([path] (dependencies path #{:clj}))
  ([path features]
   (parse-form (ns-form path features))))
