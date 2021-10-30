(ns makejack.git.impl
  (:require
   [clojure.tools.build.api :as b]))

(defn tag
  [{:keys [commit dir tag] :or {dir "."}}]
  (assert tag)
  (-> {:command-args (cond-> ["git" "tag" tag (or commit"HEAD")])
       :dir          (.getPath (b/resolve-path dir))
       :out          :capture}
      b/process
      :out))
