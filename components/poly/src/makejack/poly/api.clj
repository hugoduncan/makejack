(ns makejack.poly.api
  "Build helpers for polylith (like) monorepo projects."
  (:require
   [makejack.poly.impl :as impl]))

(defn lift-local-deps
  "Return a basis with :mvn/local deps converted to source dependencies.
  Adds transitive libs, and extends the paths."
  [basis]
  (impl/lift-local-deps basis))
