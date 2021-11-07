(ns makejack.defaults.api-test
  (:require
   [babashka.fs :as fs]
   [clojure.test :refer [deftest is testing]]
   [clojure.tools.build.api :as b]
   [makejack.defaults.api :as defaults]
   [makejack.filesystem.api :as filesystem]))

(deftest target-path-test
  (testing "target path defaults to \"target\"."
    (is (= "./target" (defaults/target-path {}))))
  (testing "target path returns the configured value."
    (is (= "abc" (defaults/target-path {:target "abc"})))))

(deftest classes-path-test
  (testing "classes path defaults to \"target\"."
    (is (= "./target/classes" (defaults/classes-path {}))))
  (testing "classes path returns the configured value."
    (is (= "./target/abc" (defaults/classes-path {:classes-dir "abc"}))))
  (testing "classes path uses the configured target path."
    (is (= "def/abc"
           (defaults/classes-path {:classes-dir "abc" :target "def"})))))

(deftest project-data-test
  (testing "project-data returns explicitly configured data"
    (let [project-data {:name 'abc/def :version "0.0.1"}]
      (is (= project-data
             (defaults/project-data project-data)))))
  (testing "project-data with no configured data throws"
    (is (thrown? java.io.FileNotFoundException
                 (defaults/project-data {}))))
  (testing "project-data with a project.edn, reads the project.edn"
    (filesystem/with-temp-dir [dir "project-data-test"]
      (let [project-data {:name 'me/abcd :version "0.0.0"}]
        (spit
         (fs/file (fs/path dir "project.edn"))
         (pr-str project-data))
        (is (= (assoc project-data :dir dir)
               (defaults/project-data {:dir dir})))))))

(deftest paths-test
  (filesystem/with-temp-dir [dir "paths-test"]
    (let [path (fs/path dir "project.edn")]
      (spit (fs/file path)
            (pr-str {:paths ["src1"]}))
      (is (= ["src1"]
             (defaults/paths (b/create-basis {:project (str path)})))))))

(deftest jar-filename-test
  (is (= "abcd-0.0.1.jar"
         (defaults/jar-filename {:name 'abcd :version "0.0.1"}))))
