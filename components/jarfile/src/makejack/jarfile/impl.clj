(ns makejack.jarfile.impl
  (:require
   [makejack.path.api :as path])
  (:import
   java.util.jar.JarFile))

(defn paths
  "Return a sequence of all the paths in the jar file."
  [jarfile-path]
  (let [jarfile (JarFile. (path/as-file jarfile-path) true JarFile/OPEN_READ)]
    (with-open [s (.stream jarfile)]
      (vec
       (for [entry (.toArray s)]
         (.getName entry))))))
