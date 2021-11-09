(ns build
  (:refer-clojure :exclude [test])
  (:require
   [makejack.build.targets :as targets]))

(targets/require
 help
 clean
 jar
 install
 changelog-init
 changelog-release
 tag-version)

(defn ^{:params []}  build
  "Build projects"
  [params]
  (-> params
      clean
      jar
      install))
