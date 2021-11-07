(ns makejack.jarfile.api-test
  (:require
   [babashka.fs :as fs]
   [clojure.test :refer [deftest is testing]]
   [clojure.tools.build.api :as b]
   [makejack.jarfile.api :as jarfile]))

;; helper so we can run tests from polylith root repl
(def dir (-> (fs/absolutize *file*)
             fs/parent
             fs/parent
             fs/parent
             fs/parent))

(deftest paths-test
  (let [jar-path (fs/path dir "target" "jarfile.jar")]
    (b/jar {:basis     (b/create-basis {:dir (str dir)})
            :class-dir (str (fs/path dir "src"))
            :jar-file  (str jar-path)})
    (testing "paths returns all paths in a jarfile"
      (let [paths (set (jarfile/paths jar-path))]
        (is (every?
             (partial contains? paths)
             ["META-INF/MANIFEST.MF"
              "makejack/jarfile/api.clj"
              "makejack/jarfile/impl.clj"])
            paths)))))
