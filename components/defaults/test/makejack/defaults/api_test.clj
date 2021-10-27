(ns makejack.defaults.api-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.tools.build.api :as b]
   [makejack.filesystem.api :as fs]
   [makejack.path.api :as path]
   [makejack.defaults.api :as defaults]))

(deftest target-path-test
  (testing "target path defaults to \"target\"."
    (is (= "target" (defaults/target-path {}))))
  (testing "target path returns the configured value."
    (is (= "abc" (defaults/target-path {:target "abc"})))))

(deftest classes-path-test
  (testing "classes path defaults to \"target\"."
    (is (= "target/classes" (defaults/classes-path {}))))
  (testing "classes path returns the configured value."
    (is (= "target/abc" (defaults/classes-path {:classes-dir "abc"}))))
  (testing "classes path uses the configured target path."
    (is (= "def/abc"
           (defaults/classes-path {:classes-dir "abc" :target "def"})))))

(deftest project-data-test
  (testing "project-data returns explicitly configured data"
    (is (= {:lib 'abc/def :version "0.0.1"}
           (defaults/project-data {:lib 'abc/def :version "0.0.1"}))))
  (testing "project-data with no configured data throws"
    (is (thrown? java.io.FileNotFoundException
                 (defaults/project-data {}))))
  (testing "project-data with a project.edn, reads the project.edn"
    (fs/with-temp-dir [dir "project-data-test"]
      (let [proj-map {:name    'me/abcd
                      :version "0.0.0"}]
        (spit
         (path/as-file (path/path dir "project.edn"))
         (pr-str proj-map))
        (is (= (update proj-map :version str "." (b/git-count-revs nil))
               (defaults/project-data {:dir dir})))))))

(deftest paths-test
  (fs/with-temp-dir [dir "paths-test"]
    (let [path (path/path dir "project.edn")]
      (spit (path/as-file path)
            (pr-str {:paths ["src1"]}))
      (is (= ["src1"]
             (defaults/paths (b/create-basis {:project (str path)})))))))

(deftest jar-filename-test
  (is (= "abcd-0.0.1.jar"
         (defaults/jar-filename {:name 'abcd :version "0.0.1"}))))
