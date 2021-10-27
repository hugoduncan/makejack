(ns makejack.project-coords.api-test
  (:require
   [clojure.edn :as edn]
   [clojure.test :refer [deftest is testing]]
   [makejack.filesystem.api :as fs]
   [makejack.path.api :as path]
   [makejack.project-coords.api :as project-coords]))

(deftest bump-version-test
  (let [params {:version "0.0.0"}]
    (is (= "0.0.1"
           (:version (project-coords/bump-version params :incremental))))
    (is (= "0.1.0"
           (:version (project-coords/bump-version params :minor))))
    (is (= "1.0.0"
           (:version (project-coords/bump-version params :major))))))

(deftest expand-test
  (doseq [c [:major :minor :incremental]]
    (testing "expand on :git-rev-count"
      (testing (str "in component" c)
        (let [version-map {c :git-rev-count}
              version     (project-coords/version-map->version version-map)
              res         (project-coords/expand
                           {:version-map version-map
                            :version     version})]
          (prn :res res)
          (is (= [c] (keys (:version-map res))))
          (is (nat-int? (get (:version-map res) c)))
          (is (string? (:version res)) "version is set")))))
  (doseq [c [:major :minor :incremental]]
    (testing "expand on :reverse-date"
      (testing c
        (let [version-map {c :reverse-date}
              version     (project-coords/version-map->version version-map)
              res         (project-coords/expand {:version-map version-map
                                                  :version     version})]
          (prn :res res)
          (is (= [c] (keys (:version-map res))))
          (is (string? (get (:version-map res) c)))
          (is (re-find #"^\d\d\d\d.\d\d.\d\d$" (get (:version-map res) c)))
          (is (re-find #"^\d\d\d\d.\d\d.\d\d$" (:version res))))))))

(deftest read-project-test
  (testing "read"
    (fs/with-temp-dir [dir "load-project-test"]
      (let [f (path/as-file (path/path dir "project.edn"))]
        (testing "with neither :version nor :version-map,"
          (spit f (pr-str {:name 'me/abcd}))
          (testing "throws"
            (is (thrown-with-msg?
                 Exception
                 #":version.*:version-map"
                 (project-coords/read {:dir dir})))))
        (testing "with no :name,"
          (spit f (pr-str {:version "0.0.0"}))
          (testing "throws"
            (is (thrown-with-msg?
                 Exception
                 #":name"
                 (project-coords/read {:dir dir})))))
        (testing "with only a :version,"
          (spit f (pr-str {:name 'me/abcd :version "0.0.0"}))
          (testing "returns both :version and :version-map"
            (is (= {:name        'me/abcd
                    :version     "0.0.0"
                    :version-map {:major 0 :minor 0 :incremental 0}}
                   (project-coords/read {:dir dir})))))
        (testing "with only a :version-map,"
          (spit
           f
           (pr-str {:name        'me/abcd
                    :version-map {:major 0 :minor 0 :incremental 0}}))
          (testing "returns both :version and :version-map"
            (is (= {:name        'me/abcd
                    :version     "0.0.0"
                    :version-map {:major 0 :minor 0 :incremental 0}}
                   (project-coords/read {:dir dir})))))
        (testing "with only a :version-map,"
          (spit
           f
           (pr-str {:name        'me/abcd
                    :version-map {:major 0 :minor 0 :incremental 0}}))
          (testing "returns both :version and :version-map"
            (is (= {:name        'me/abcd
                    :version     "0.0.0"
                    :version-map {:major 0 :minor 0 :incremental 0}}
                   (project-coords/read {:dir dir})))))
        (testing "with a :git-rev-count,"
          (spit
           f
           (pr-str {:name        'me/abcd
                    :version-map {:major       0
                                  :minor       0
                                  :incremental :git-rev-count}}))
          (testing "returns both :version and :version-map"
            (is (= {:name        'me/abcd
                    :version     "0.0.:git-rev-count"
                    :version-map {:major       0
                                  :minor       0
                                  :incremental :git-rev-count}}
                   (project-coords/read {:dir dir})))))))))


(deftest write-project-test
  (fs/with-temp-dir [dir "load-project-test"]
    (testing "with no existing project map"
      (project-coords/write {:dir dir :name 'me/z :version "0.0.1"})
      (is (= {:name 'me/z :version "0.0.1"}
             (-> (path/path dir "project.edn")
                 path/as-file
                 slurp
                 edn/read-string)))
      (is (= {:name        'me/z
              :version     "0.0.1"
              :version-map {:major 0, :minor 0, :incremental 1}}
             (project-coords/read {:dir dir}))))
    (testing "with existing project map with :version"
      (spit
       (path/as-file (path/path dir "project.edn"))
       (pr-str {:name    'me/abcd
                :version "0.0.0"}))
      (project-coords/write {:dir dir :name 'me/z :version "0.0.1"})
      (is (= {:name 'me/z :version "0.0.1"}
             (-> (path/path dir "project.edn")
                 path/as-file
                 slurp
                 edn/read-string))
          "wrote :version")
      (is (= {:name        'me/z
              :version     (str "0.0.1")
              :version-map {:major 0, :minor 0, :incremental 1}}
             (project-coords/read {:dir dir}))))
    (testing "with existing project map with :version-map"
      (spit
       (path/as-file (path/path dir "project.edn"))
       (pr-str {:name        'me/abcd
                :version-map {:major "0" :minor "0" :incremental "0"}}))
      (project-coords/write {:dir dir :name 'me/z :version "0.0.1"})
      (is (= {:name        'me/z
              :version-map {:major "0", :minor "0", :incremental "1"}}
             (-> (path/path dir "project.edn")
                 path/as-file
                 slurp
                 edn/read-string))
          "wrote :version-map")
      (is (= {:name        'me/z
              :version     (str "0.0.1")
              :version-map {:major "0", :minor "0", :incremental "1"}}
             (project-coords/read {:dir dir}))))))
