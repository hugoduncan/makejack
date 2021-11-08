(ns makejack.deps.api
  (:require
   [makejack.deps.impl :as impl]))

(defn lift-local-deps
  "Return a basis with :mvn/local deps converted to source dependencies.
  Adds transitive libs, and extends the paths."
  [basis]
  (impl/lift-local-deps basis))
