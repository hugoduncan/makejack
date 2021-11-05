(ns mj.test.default-test
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [makejack.build.targets :as targets]
   [makejack.filesystem.api :as fs]
   [makejack.jarfile.api :as jarfile]
   [makejack.path.api :as path]))

;; helper so we can run tests from polylith root repl
(def dir (or (some-> (path/path *file*)
                     path/parent
                     path/parent
                     path/parent
                     path/parent)
             (path/path (System/getProperty "user.dir"))))

(deftest help-test
  (testing "help prints a help string"
    (is (re-matches #"(?s)\s*help  Show help\n.*"
                    (with-out-str
                      (targets/help {})))
        "starts with the help target")

    (is (< 10 (count
               (str/split-lines
                (with-out-str
                  (targets/help {})))))
        "has many lines")))

(deftest clean-test
  (testing "clean removes the target directory"
    (let [path (path/path dir "target")]
      (fs/mkdirs path)
      (is (fs/file-exists? path))
      (is (= {:dir (str dir)} (targets/clean {:dir (str dir)})) "runs")
      (is (not (fs/file-exists? path))))))

(deftest jar-test
  (testing "jar creates a jar file containing the sources and resources"
    (targets/clean {:dir (str dir)})
    (let [jar-path (path/path "target" "default-0.1.jar")
          path     (path/path dir jar-path)]
      (is (= {:name     'mj.test/default
              :version  "0.1"
              :dir      (str dir)
              :jar-file (str path)}
             (targets/jar {:dir (str dir)}))
          "runs")

      (is (fs/file-exists? path))
      (let [paths    (set (jarfile/paths path))
            expected ["META-INF/MANIFEST.MF"
                      "META-INF/maven/mj.test/default/pom.xml"
                      "META-INF/maven/mj.test/default/pom.properties"
                      "mj/test/default.clj"
                      "a-resource.edn"]
            extra    (set/difference paths (set expected))
            missing  (vec (remove (partial contains? paths) expected))]
        (is (empty? missing) (str "Missing: " missing))
        (is (every? #(.endsWith % "/") extra) (str "Extra: " extra))))))

(deftest compile-clj-test
  (testing "compile-clj compiles the sources files into target/classes"
    (targets/clean {:dir (str dir)})
    (let [classes-path (path/path dir "target" "classes")]
      (is (= {:dir       (str dir)
              :class-dir (str classes-path)}
             (targets/compile-clj {:dir (str dir)}))
          "runs")

      (is (fs/file-exists? classes-path))
      (is (fs/file-exists?
           (path/path classes-path "mj" "test" "default__init.class"))))))

(deftest uber-test
  (testing "compile-clj compiles the sources files into target/classes"
    (targets/clean {:dir (str dir)})
    (let [classes-path (path/path dir "target" "classes")
          jar-path     (path/path "target" "default-0.1.jar")
          path         (path/path dir jar-path)]
      (targets/compile-clj {:dir (str dir)})
      (is (= {:name     'mj.test/default
              :version  "0.1"
              :dir      (str dir)
              :jar-file (str path)}
             (targets/uber {:dir (str dir)}))
          "runs")

      (is (fs/file-exists? path))
      (let [paths    (set (jarfile/paths path))
            expected ["META-INF/MANIFEST.MF"
                      "META-INF/maven/mj.test/default/pom.xml"
                      "META-INF/maven/mj.test/default/pom.properties"
                      "mj/test/default.clj"
                      "mj/test/default__init.class"
                      "a-resource.edn"]
            extra    (set/difference paths (set expected))
            missing  (vec (remove (partial contains? paths) expected))]
        (is (empty? missing) (str "Missing: " missing))
        (is (every? #(or (.endsWith % "/")
                         (re-matches #"mj/test/default.*class" %))
                    extra)
            (str "Extra: " extra))))))
