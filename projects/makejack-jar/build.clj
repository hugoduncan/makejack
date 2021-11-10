(ns build
  (:require
   [makejack.tasks :as tasks]))

(tasks/require
 help
 clean
 jar
 install
 changelog-release
 tag-version)

(defn ^{:params []}  build
  "Build projects"
  [params]
  (-> params
      clean
      jar
      install))
