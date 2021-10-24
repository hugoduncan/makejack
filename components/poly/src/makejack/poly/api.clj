(ns makejack.poly.api
  "Build helpers for polylith (like) monorepo projects"
  (:require
   [clojure.tools.build.api :as b]
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
(defn lift-local-deps
  "Return a basis with :mvn/local deps converted to source dependencies.
  Adds transitive libs, and extends the paths."
  [basis]
  (let [transitive-paths []
        transitive-deps  {}]
    (-> (b/create-basis {:extra {:deps  transitive-deps
                                 :paths transitive-paths}}))))
