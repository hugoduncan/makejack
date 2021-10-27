(ns makejack.project-coords.api-test
  (:require
   [clojure.edn :as edn]
   [clojure.test :refer [deftest is testing]]
   [makejack.filesystem.api :as fs]
   [makejack.path.api :as path]
   [makejack.project-coords.api :as project-coords]
   [makejack.project-coords.impl :as impl]))

(deftest bump-version-test
  (let [params {:version "0.0.0"}]
    (is (= "0.0.1"
           (:version (project-coords/bump-version params :incremental))))
    (is (= "0.1.0"
           (:version (project-coords/bump-version params :minor))))
    (is (= "1.0.0"
           (:version (project-coords/bump-version params :major))))))

(deftest expand-version-test
  (doseq [c [:major :minor :incremental]]
    (testing "expand on :git-rev-count"
      (testing (str "in component" c)
        (let [version (impl/version-map->version {c :git-rev-count})
              res     (project-coords/expand-version {:version version})]
          (is (string? (:version res)) "version is set")
          (is (nat-int? (edn/read-string (:version res))))))))
  (doseq [c [:major :minor :incremental]]
    (testing "expand on :reverse-date"
      (testing c
        (let [version (impl/version-map->version {c :reverse-date})
              res     (project-coords/expand-version {:version version})]
          (is (re-find #"^\d\d\d\d.\d\d.\d\d$" (:version res))))))))

(deftest read-project-test
  (testing "read"
    (fs/with-temp-dir [dir "load-project-test"]
      (let [f (path/as-file (path/path dir "project.edn"))]
        (testing "with no :version,"
          (spit f (pr-str {:name 'me/abcd}))
          (testing "throws"
            (is (thrown-with-msg?
                 Exception
                 #":version"
                 (project-coords/read {:dir dir})))))
        (testing "with no :name,"
          (spit f (pr-str {:version "0.0.0"}))
          (testing "throws"
            (is (thrown-with-msg?
                 Exception
                 #":name"
                 (project-coords/read {:dir dir})))))
        (testing "with a :version,"
          (let [project-data {:name 'me/abcd :version "0.0.0"}]
            (spit f (pr-str project-data))
            (testing "returns both :version"
              (is (= project-data
                     (project-coords/read {:dir dir}))))))
        (testing "with a :git-rev-count,"
          (let [project-data {:name    'me/abcd
                              :version "0.0.:git-rev-count"}]
            (spit f (pr-str project-data))
            (testing "returns :version"
              (is (= project-data
                     (project-coords/read {:dir dir}))))))))))


(deftest write-project-test
  (fs/with-temp-dir [dir "load-project-test"]
    (testing "with no existing project map"
      (let [project-data {:name 'me/z :version "0.0.1"}]
        (project-coords/write (assoc project-data :dir dir))
        (is (= project-data
               (-> (path/path dir "project.edn")
                   path/as-file
                   slurp
                   edn/read-string)))
        (is (= project-data (project-coords/read {:dir dir})))))
    (testing "with existing project map with :version"
      (let [project-data {:name 'me/abcd :version "0.0.0"}]
        (spit
         (path/as-file (path/path dir "project.edn"))
         (pr-str project-data))
        (project-coords/write (assoc project-data :dir dir))
        (is (= project-data
               (-> (path/path dir "project.edn")
                   path/as-file
                   slurp
                   edn/read-string))
            "wrote :version")
        (is (= project-data (project-coords/read {:dir dir})))))))
