(ns makejack.tasks.poly
  "Tasks for working with polylith projects."
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.files.api :as files]
   [makejack.git.api :as git]
   [makejack.poly.api :as poly]
   [makejack.project-data.api :as project-data]
   [makejack.target-doc.api :as target-doc]
   [makejack.verbose.api :as v]))


(defn poly
  "Run a mj target on a polylith project.

  Unrecognised options are passed to the target.

  Options:
    - :on     as per `poly test`, e.g :project
    - :task   the task to invoke (a clojure function symbol)."
  [params]
  (let [ws      (poly/workspace params)
        task    (:task params)
        changes (poly/resolve-elements ws (:on params ":project"))]
    (prn :task task :on (:on params) :changes changes)
    (doseq [change changes]
      ((requiring-resolve task) (assoc params :dir change)))))

(defn poly-clj-kondo
  "Run clj-kondo on a polylith project."
  [params]
  (let [{:keys [ws-dir] :as ws} (poly/workspace params)
        changes                 (poly/changed-elements ws)
        config-dir              (str (fs/path ws-dir ".clj-kondo"))]
    (doseq [change changes]
      (clj-kondo (merge params {:dir change :config-dir config-dir})))))

(defn poly-apply
  "Run clojure on a polylith project.
  Aliases are defined in the workspace level deps.edn."
  [params]
  (let [{:keys [ws-dir] :as ws} (poly/workspace params)
        aliases                 (:aliases params)
        apply-kind              (:apply-kind params)
        changes                 (poly/changed-elements ws)
        basis                   (defaults/basis params)
        alias-maps              (select-keys (:aliases basis) aliases)
        args                    ["clojure"
                                 "-Sdeps" (pr-str {:aliases alias-maps})
                                 (str apply-kind (str/join aliases))]]
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
  (let [{:keys [ws-dir] :as ws} (poly/workspace params)
        aliases                 (:aliases params)
        changes                 (poly/changed-elements ws)
        basis                   (defaults/basis params)
        alias-maps              (select-keys (:aliases basis) aliases)
        args                    ["clojure"
                                 "-Sdeps" (pr-str {:aliases alias-maps})
                                 (str "-M" (str/join aliases))]]
    (prn :args args)
    (doseq [change changes]
      (prn :change change)
      (b/process (merge
                  params
                  {:dir          change
                   :out          :inherit
                   :command-args args})))))

(defn poly-exec
  "Run clojure -X on a polylith project.
  Aliases are defined in the workspace level deps.edn."
  [params]
  (let [{:keys [ws-dir] :as ws} (poly/workspace params)
        aliases                 (:aliases params)
        changes                 (poly/changed-elements ws)
        basis                   (defaults/basis params)
        alias-maps              (select-keys (:aliases basis) aliases)
        args                    ["clojure"
                                 "-Sdeps" (pr-str {:aliases alias-maps})
                                 (str "-M" (str/join aliases))]]
    (prn :args args)
    (doseq [change changes]
      (prn :change change)
      (b/process (merge
                  params
                  {:dir          change
                   :out          :inherit
                   :command-args args})))))
