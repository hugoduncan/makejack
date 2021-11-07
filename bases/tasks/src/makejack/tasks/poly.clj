(ns makejack.tasks.poly
  "Tasks for working with polylith projects."
  (:require
   [makejack.poly.api :as poly]))

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
