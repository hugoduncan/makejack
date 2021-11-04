(ns makejack.jarfile.api-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.tools.build.api :as b]
   [makejack.jarfile.api :as jarfile]
   [makejack.path.api :as path]))

;; helper so we can run tests from polylith root repl
(def dir (-> (path/path *file*)
             path/parent
             path/parent
             path/parent
             path/parent))

(deftest paths-test
  (let [jar-path (path/path dir "target" "jarfile.jar")]
    (b/jar {:basis     (b/create-basis {:dir (str dir)})
            :class-dir (str (path/path dir "src"))
            :jar-file  (str jar-path)})
    (testing "paths returns all paths in a jarfile"
      (let [paths (set (jarfile/paths jar-path))]
        (is (every?
             (partial contains? paths)
             ["META-INF/MANIFEST.MF"
              "makejack/jarfile/api.clj"
              "makejack/jarfile/impl.clj"])
            paths)))))
