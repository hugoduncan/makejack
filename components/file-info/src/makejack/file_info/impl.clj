(ns makejack.file-info.impl
  (:require
   [makejack.file-info.namespace-deps :as namespace-deps]))

(defn dependencies
  ([path] (dependencies path #{:clj}))
  ([path features]
   (namespace-deps/dependencies path)))
