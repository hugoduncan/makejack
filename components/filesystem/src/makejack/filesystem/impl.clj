(ns makejack.filesystem.impl
  "File system implementation details."
  (:require
   [babashka.fs :as fs]))

(defn ^:no-doc with-bindings-macro
  [bindings body macro-sym macro-fn]
  {:pre [(vector? bindings) (even? (count bindings))]}
  (cond
    (not (seq bindings))   `(do ~@body)
    (symbol? (bindings 0)) (macro-fn
                            (subvec bindings 0 2)
                            [`(~macro-sym
                               ~(subvec bindings 2)
                               ~@body)])
    :else                  (throw
                            (IllegalArgumentException.
                             (str (name macro-sym)
                                  " only allows [symbol value] pairs in bindings")))))

(defn ^:no-doc with-temp-dir-fn
  [[sym options] body]
  `(let [~sym (fs/create-temp-dir ~options)]
     (try
       ~@body
       (finally
         (fs/delete-tree ~sym)))))
