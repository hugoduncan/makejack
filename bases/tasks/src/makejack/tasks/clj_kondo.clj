(ns makejack.tasks.clj-kondo
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.verbose.api :as v]))

(defn clj-kondo
  "lint with clj-kondo."
  [{:keys [cache-dir config-dir dir] :or {dir "."} :as params}]
  (v/println params "clj-kondo" dir "...")
  (let [basis        (defaults/basis params)
        paths        (if (:init params)
                       (keys (:classpath basis))
                       (:paths basis))
        command-args (cond-> ["clj-kondo" "--lint" (str/join ":" paths)]
                       (:init params)
                       (into ["--dependencies" "--parallel" "--copy-configs"])
                       config-dir
                       (into ["--config-dir" config-dir])
                       cache-dir
                       (into ["--cache-dir" cache-dir]))]
    (->
     {:command-args command-args
      :dir          (.getPath (b/resolve-path dir))
      :out          :inherit}
     b/process)
    params))
