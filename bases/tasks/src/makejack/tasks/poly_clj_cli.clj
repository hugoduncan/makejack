(ns makejack.tasks.poly-clj-cli
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.poly.api :as poly]))

(defn- poly-apply
  "Run clojure on a polylith project.
  Aliases are defined in the workspace level deps.edn."
  [params switch-str]
  (let [ ws        (poly/workspace params)
        aliases    (:aliases params)
        changes    (poly/changed-elements ws)
        basis      (defaults/basis params)
        alias-maps (select-keys (:aliases basis) aliases)
        args       ["clojure"
                    "-Sdeps" (pr-str {:aliases alias-maps})
                    (str switch-str (str/join aliases))]]
    (prn :args args)
    (doseq [change changes]
      (prn :change change)
      (b/process (merge
                  params
                  {:dir          change
                   :out          :inherit
                   :command-args args})))))

(defn poly-main
  "Run clojure -M on a polylith project.
  Aliases are defined in the workspace level deps.edn."
  [params]
  (poly-apply params "-M"))

(defn poly-exec
  "Run clojure -X on a polylith project.
  Aliases are defined in the workspace level deps.edn."
  [params]
  (poly-apply params "-X"))
