(ns makejack.tasks.poly-clj-cli
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.poly.api :as poly]
   [makejack.verbose.api :as v]))

(defn- poly-apply
  "Run clojure on a polylith project.
  Aliases are defined in the workspace level deps.edn."
  [params switch-str]
  (let [ws         (poly/workspace params)
        aliases    (:aliases params)
        changes    (poly/changed-elements ws)
        basis      (defaults/basis params)
        alias-maps (select-keys (:aliases basis) aliases)
        cmd-args   (->
                    ["clojure"]
                    (cond->
                        (not (:no-propagation params))
                      (into ["-Sdeps" (pr-str {:aliases alias-maps})]))
                    (conj (str switch-str (str/join aliases)))
                    (into  (:args params)))]
    (doseq [change changes]
      (v/println params change)
      (when (fs/exists? change)
        (apply v/println params cmd-args)
        (b/process (merge
                    params
                    {:dir          change
                     :out          :inherit
                     :command-args cmd-args}))))))

(defn poly-main
  "Run clojure -M on a polylith project.
  Aliases are defined in the workspace level deps.edn."
  [params]
  (poly-apply params "-M"))

(defn poly-exec
  "Run clojure -X on a polylith project.
  Aliases are defined in the workspace level deps.edn."
  [params]
  (poly-apply (update params :args #(apply concat %)) "-X"))

(defn poly-tool
  "Run clojure -T on a polylith project.
  Aliases are defined in the workspace level deps.edn."
  [params]
  (poly-apply
   (assoc params :args
          (into [(str (:exec-fn params))]
                (apply concat (:exec-args params))))
   "-T"))
