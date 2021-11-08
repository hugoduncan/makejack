(ns build
  (:refer-clojure :exclude [test])
  (:require
   [clojure.tools.build.api :as b]
   [makejack.build.targets :as targets]))


(targets/require
 help)

(defn ^{:params []}  build
  "Build projects"
  [params]
  (targets/poly-tool
   (merge params {:aliases        [:build]
                  :exec-fn        'build
                  :exec-args      {}
                  :no-propagation true})))

(defn ^{:params []} clean
  "Clean projects"
  [params]
  (targets/poly-tool
   (merge params {:aliases        [:build]
                  :exec-fn        'clean
                  :exec-args      {}
                  :no-propagation true})))

(defn ^{:params []} cljfmt
  "Run `cljfmt check` on workspac"
  [params]
  (targets/poly-main {:aliases [:cljfmt] :args ["check"]}))

(defn ^{:params []} cljfmt-fix
  "Run `cljfmt fix` on workspace"
  [params]
  (targets/poly-main {:aliases [:cljfmt] :args ["check"]}))

(defn ^{:params []} test
  "Run `poly test` on workspace.

  Note: you can run `poly test` directly."
  [params]
  (b/process {:command-args ["poly" "test"] :out :inherit}))