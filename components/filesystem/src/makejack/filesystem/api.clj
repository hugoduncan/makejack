(ns makejack.filesystem.api
  "File system manipulation functions."
  (:require
   [makejack.filesystem.impl :as impl]))

(defmacro with-temp-dir
  "bindings => [name prefix-or-options ...]

  Evaluate body with names bound to java.io.File
  objects of newly created temporary directories, and a finally clause
  that deletes them recursively in reverse order.

  Prefix is a string that is used to name the directory.
  Options is a map with the keys:
  :delete-on-exit - delete the dir on JVM exit (default true)
  :dir - directory to create the dir in (default is the system temp dir).
         Must be of type that can be passed to clojure.java.io/dir.
  :prefix - a string that is used to name the directory."
  [bindings & body]
  {:pre [(vector? bindings) (even? (count bindings))]}
  (impl/with-bindings-macro bindings body `with-temp-dir impl/with-temp-dir-fn))
