(ns build
  (:refer-clojure :exclude [test])
  (:require
   [clojure.tools.build.api :as b]
   [makejack.build.targets :as targets]))


(targets/require
 help
 clean
 jar
 install)

(defn ^{:params []}  build
  "Build projects"
  [params]
  (-> params
      clean
      jar
      install))
