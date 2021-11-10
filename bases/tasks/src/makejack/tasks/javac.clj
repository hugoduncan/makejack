(ns makejack.tasks.javac
  (:require
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.verbose.api :as v]))

(defn javac
  "Java complilation"
  [params]
  (v/println params "compile-java...")
  (let [basis (defaults/basis params)]
    (b/javac
     {:class-dir  (defaults/classes-path params)
      :basis      basis
      :src-dirs   (:java-paths basis)
      :javac-opts (:javac-opts params)})))
