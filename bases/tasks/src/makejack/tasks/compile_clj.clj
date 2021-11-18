(ns makejack.tasks.compile-clj
  (:require
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.deps.api :as deps]
   [makejack.files.api :as files]
   [makejack.verbose.api :as v]))

(defn compile-clj
  "AOT complilation"
  [params]
  (v/println params "compile-clj...")
  (let [basis     (deps/lift-local-deps (defaults/basis params))
        class-dir (defaults/classes-path params)
        info-map  (files/info-map params (defaults/paths basis))
        _         (assert (some? info-map))
        nses      (files/topo-namespaces info-map)]
    (binding [b/*project-root* (:dir params ".")]
      (b/compile-clj
       {:class-dir  class-dir
        :basis      basis
        :ns-compile (remove #(re-matches #"hooks.*" (str %)) nses) }))
    (assoc params :class-dir class-dir)))
