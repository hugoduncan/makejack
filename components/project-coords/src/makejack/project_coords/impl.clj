(ns makejack.project-coords.impl
  (:require
   [aero.core :as aero]
   [clojure.edn :as edn]
   [makejack.util.filesystem :as fs]
   [makejack.util.path :as path]))

(defn- maybe-path
  "Return a path from source if source can be understood as a path."
  [source]
  (try
    (path/as-path source)
    (catch java.lang.IllegalArgumentException _ nil)))

(defn relative-to-resolver
  "Resolves relative to the source file, or to the given directory."
  [dir]
  (fn relative-to-resolver [source include]
    (let [fl (if (path/absolute? include)
               include
               (if-let [source-path (maybe-path source)]
                 (if-let [parent (path/parent source-path)]
                   (path/path parent include)
                   include)
                 (if dir
                   (path/path dir include)
                   include)))]
      (if (and fl (fs/file-exists? fl))
        (path/as-file fl)
        (java.io.StringReader. (pr-str {:aero/missing-include include}))))))

(def project-with-defaults
  ;; project-project is the project's project.edn, as is
  (array-map
   :project-project (tagged-literal 'include "project.edn")
   ;; project is the project's project.edn, with some defaults
   :project-p1      (tagged-literal
                     'merge
                     [(tagged-literal 'ref [:project-project])
                      {:group-id
                       (tagged-literal
                        'or
                        [(tagged-literal 'opt-ref [:project-project :group-id])
                         (tagged-literal 'ref [:project-project :name])])
                       :artifact-id
                       (tagged-literal
                        'or
                        [(tagged-literal 'opt-ref [:project-project :artifact-id])
                         (tagged-literal 'ref [:project-project :name])])
                       :jar-type
                       (tagged-literal
                        'or
                        [(tagged-literal 'opt-ref [:project-project :jar-type])
                         :jar])}])
   :project         (tagged-literal
                     'merge
                     [(tagged-literal 'ref [:project-p1])
                      {:jar-name
                       (tagged-literal
                        'or
                        [(tagged-literal 'opt-ref [:project-p1 :jar-name])
                         (tagged-literal
                          'default-jar-name
                          [(tagged-literal 'ref [:project-p1 :artifact-id])
                           (tagged-literal 'ref [:project-p1 :version])
                           (tagged-literal 'ref [:project-p1 :jar-type])])])}])))



(defn load-project* [& [{:keys [dir] :as options}]]
  (edn/read-string
   (slurp (path/as-file (path/path dir "project.edn"))))
  ;; (:project
  ;;  (aero/read-config
  ;;   (java.io.StringReader.
  ;;    (pr-str project-with-defaults))
  ;;   (merge
  ;;    {:resolver (relative-to-resolver dir)}
  ;;    options)))
  )


(defn bump-version [options])
