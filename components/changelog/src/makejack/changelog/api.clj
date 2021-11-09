(ns makejack.changelog.api
  (:require
   [makejack.changelog.impl :as impl]))

(defn changelog
  [params]
  (impl/changelog params))
