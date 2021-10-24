(ns makejack.poly.api
  "Build task helpers for polylith (like) projects"
  (:require
   [makejack.filesystem.api :as fs]))


(defn classpath-directory-roots
  "Extract all directory roots in the classpath"
  [basis]
  (->> basis
       :classpath-roots
       (filter fs/directory?)))

(defn without-local-deps
  "Extract all directory roots in the classpath"
  [basis]
  (-> basis
      (update :libs
              #(apply dissoc % (->> basis
                                    :libs
                                    (filter (comp :local/root val))
                                    (mapv key))))
      (update :extra-deps
              #(apply dissoc % (->> basis
                                    :extra-deps
                                    (filter (comp :local/root val))
                                    (mapv key))))))
