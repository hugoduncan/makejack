(ns makejack.changelog.impl
  (:require
   [changelog.main :as changlog]))

(defn changelog
  [params]
  (changlog/run
    {:task    (name (:task params))
     :version (:version params)}))
