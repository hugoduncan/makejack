(ns makejack.tasks.poly-clj-kondo
  (:require
   [babashka.fs :as fs]
   [makejack.poly.api :as poly]
   [makejack.tasks.clj-kondo :as clj-kondo]))

(defn poly-clj-kondo
  "Run clj-kondo on a polylith project."
  [params]
  (let [{:keys [ws-dir] :as ws} (poly/workspace params)
        changes                 (poly/changed-elements ws)
        config-dir              (str (fs/path ws-dir ".clj-kondo"))]
    (doseq [change changes]
      (clj-kondo/clj-kondo
       (merge params {:dir change :config-dir config-dir})))))
