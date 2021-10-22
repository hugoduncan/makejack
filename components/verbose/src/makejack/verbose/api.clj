(ns makejack.verbose.api
  "Micro component for printing based on a :verbose param."
  (:refer-clojure :exclude [println])
  (:require
   [makejack.verbose.impl :as impl]))

(defn println
  "Optionally print when params has a truthy :verbose key.
  args are forwarded to println."
  {:arglists '[[{:keys [verbose] :as params} & args]]}
  [params & args]
  (impl/println-when-verbose params args))
