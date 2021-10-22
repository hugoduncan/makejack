(ns makejack.build.targets
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [makejack.poly.api :as poly]
   [makejack.project-coords.api :as project-coords]
   [makejack.target-doc.api :as target-doc]))

(def lib 'makejack/makejack)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")


(defn clean
  "Remove all built files"
  [_]
  (b/delete {:path "target"}))

(def jar-ignores
  [".*~$" "^#.*#$" "^\\.#.*" "^.DS_Store$" "^.keep$"])

(defn jar
  "Build a jar file"
  [_]
  (let [coords    (project-coords/load {:dir "."})
        lib       (symbol (format "%s/%s" (:group-id coords) (:name coords)))
        rev-count (b/git-count-revs nil)
        version   (format
                   "%s.%s"
                   (:version coords)
                   (if (str/blank? rev-count) "0" rev-count))
        jar-file  (format "target/%s-%s.jar" (name lib) version)
        basis     (b/create-basis {:project "deps.edn"})
        src-dirs  (poly/classpath-directory-roots basis)]
    (b/write-pom {:class-dir class-dir
                  :lib       lib
                  :version   version
                  :basis     (poly/without-local-deps basis)
                  :src-dirs  src-dirs})
    (b/copy-dir {:src-dirs   src-dirs
                 :target-dir class-dir
                 :ignores    jar-ignores})
    (b/jar {:class-dir class-dir
            :jar-file  jar-file})))

(defn ^{:doc-order 0 :params "[:target target-name]"} help
  "Show help

  Use :target to get detailed help on a specific target."
  [params]
  ;; TODO: make this a function in target-doc
  (println)
  (println (target-doc/help-task params 'makejack.build.targets)))
