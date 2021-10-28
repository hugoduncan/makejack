(ns makejack.project-data.api-test
  (:require
   [clojure.edn :as edn]
   [clojure.test :refer [deftest is testing]]
   [makejack.filesystem.api :as fs]
   [makejack.path.api :as path]
   [makejack.project-data.api :as project-data]
   [makejack.project-data.impl :as impl]))

(deftest bump-version-test
  (let [params {:version "0.0.0"}]
    (testing "bump-version increments the given version component"
      (is (= "0.0.1"
             (:version (project-data/bump-version params :incremental))))
      (is (= "0.1.0"
             (:version (project-data/bump-version params :minor))))
      (is (= "1.0.0"
             (:version (project-data/bump-version params :major)))))))

(deftest expand-version-test
  (testing "expand-version"
    (doseq [c [:major :minor :incremental]]
      (testing "on :git-rev-count produces a integer version component"
        (let [version (impl/version-map->version {c :git-rev-count})
              res     (project-data/expand-version {:version version})]
          (is (string? (:version res)) c)
          (is (nat-int? (edn/read-string (:version res))) c))))
    (doseq [c [:major :minor :incremental]]
      (testing "on :reverse-date produces a YYYY.MM.DD format string"
        (let [version (impl/version-map->version {c :reverse-date})
              res     (project-data/expand-version {:version version})]
          (is (re-find #"^\d\d\d\d.\d\d.\d\d$" (:version res)) c))))))

(deftest read-project-test
  (testing "read"
    (fs/with-temp-dir [dir "load-project-test"]
      (let [f (path/as-file (path/path dir "project.edn"))]
        (testing "throws with no :version project data"
          (spit f (pr-str {:name 'me/abcd}))
          (is (thrown-with-msg?
               Exception
               #":version"
               (project-data/read {:dir dir}))))
        (testing "throws with no :name project data"
          (spit f (pr-str {:version "0.0.0"}))
          (is (thrown-with-msg?
               Exception
               #":name"
               (project-data/read {:dir dir}))))
        (testing "reads project data with a literal :version string"
          (let [project-data {:name 'me/abcd :version "0.0.0"}]
            (spit f (pr-str project-data))
            (is (= project-data
                   (project-data/read {:dir dir})))))
        (testing "reads project data with a :git-rev-count version string,"
          (let [project-data {:name    'me/abcd
                              :version "0.0.:git-rev-count"}]
            (spit f (pr-str project-data))
            (is (= project-data
                   (project-data/read {:dir dir})))))))))


(deftest write-project-test
  (testing "write"
    (fs/with-temp-dir [dir "load-project-test"]
      (testing "saves project data with no existing project.edn file"
        (let [project-data {:name 'me/z :version "0.0.1"}]
          (project-data/write (assoc project-data :dir dir))
          (is (= project-data
                 (-> (path/path dir "project.edn")
                     path/as-file
                     slurp
                     edn/read-string)))
          (is (= project-data (project-data/read {:dir dir})))))
      (testing "updates project data tn an existing project.edn file"
        (let [project-data {:name 'me/abcd :version "0.0.0"}]
          (spit
           (path/as-file (path/path dir "project.edn"))
           (pr-str project-data))
          (project-data/write (assoc project-data :dir dir))
          (is (= project-data
                 (-> (path/path dir "project.edn")
                     path/as-file
                     slurp
                     edn/read-string))
              "wrote :version")
          (is (= project-data (project-data/read {:dir dir}))))))))
