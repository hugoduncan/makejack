(ns build
  (:refer-clojure :exclude [test])
  (:require
   [clojure.tools.build.api :as b]
   [makejack.tasks :as tasks]))

(tasks/require
 help)

(defn ^{:params []}  build
  "Build projects"
  [params]
  (tasks/poly-tool
   (merge params {:aliases        [:build]
                  :exec-fn        'build
                  :exec-args      {}
                  :no-propagation true
                  :elements       [:projects]})))

(defn ^{:params []} clean
  "Clean projects"
  [params]
  (tasks/poly-tool
   (merge params {:aliases        [:build]
                  :exec-fn        'clean
                  :exec-args      {}
                  :no-propagation true})))

(defn ^{:params []} cljfmt
  "Run `cljfmt check` on workspace"
  [params]
  (tasks/poly-main
   (merge
    (select-keys params [:verbose])
    {:aliases [:cljfmt] :args ["check"]})))

(defn ^{:params []} cljfmt-fix
  "Run `cljfmt fix` on workspace"
  [params]
  (merge
   (select-keys params [:verbose])
   (tasks/poly-main {:aliases [:cljfmt] :args ["check"]})))

(defn ^{:params []} test
  "Run `poly test` on workspace.

  Note: you can run `poly test` directly."
  [_params]
  (b/process {:command-args ["poly" "test"] :out :inherit}))
