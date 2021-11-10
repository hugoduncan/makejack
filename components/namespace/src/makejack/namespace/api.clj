(ns makejack.namespace.api
  (:require
   [makejack.namespace.impl :as impl]))

(defn ns-form
  "Return the namespace form fromt the path, using the given reader features."
  ([path] (ns-form path #{:clj}))
  ([path features]
   (impl/ns-form path features)))

(defn declared-ns
  "Return the namespace decalres in the ns form."
  [form]
  (impl/declared-ns form))

(defn parse
  "Extract the dependencies of a namespace form.

  Return a map with:
    - :namespace   the declared namespace, a symbol
    - :requires    the required namespaces, a list of maps with
                   :namespace, :as and :refer keys.
    - :imports     the imported classes, a list of symbols
  are symbols."
  [form]
  (impl/parse form))

(defn dependencies
  "Return a map with :requires, and imports for the given clj path"
  [path]
  (impl/dependencies path))
