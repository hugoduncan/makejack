(ns makejack.poly.api
  "Build helpers for polylith (like) monorepo projects."
  (:require
   [makejack.poly.impl :as impl]))

(defn workspace [params]
  (impl/workspace params))

(defn all-elements [ws]
  (impl/all-elements ws))

(defn changed-elements [ws]
  (impl/changed-elements ws))

(defn resolve-elements
  "Given a polylith spec string, return paths to the resolved elements.
  "
  [ws target-spec]
  (impl/resolve-elements ws target-spec))
