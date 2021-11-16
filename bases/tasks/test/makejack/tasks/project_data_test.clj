(ns makejack.tasks.project-data-test
  (:require
   [babashka.fs :as fs]
   [clojure.test :refer [deftest is testing]]
   [makejack.tasks.project-data :as project-data]
   [makejack.poly.api :as poly]))


(def ws (poly/workspace {:keys [:ws-dir]}))

(def dir (fs/path
          (:ws-dir ws)
          "bases"
          "tasks"))

(deftest project-data-test
  (testing "loading the project data adds the name and version keys"
    (let [v (project-data/project-data {:dir (fs/path dir "test-resources")})]
      (is (= 'project-data/test (:name v)))
      (is (= "1.2.3" (:version v))))))
