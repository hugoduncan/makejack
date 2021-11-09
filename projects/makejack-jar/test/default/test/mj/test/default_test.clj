(ns mj.test.default-test
  (:require
   [babashka.fs :as fs]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [makejack.tasks :as tasks]
   [makejack.jarfile.api :as jarfile]))

;; helper so we can run tests from polylith root repl
(def dir (or (some-> (fs/path *file*)
                     fs/parent
                     fs/parent
                     fs/parent
                     fs/parent)
             (fs/path (System/getProperty "user.dir"))))

(deftest help-test
  (testing "help prints a help string"
    (is (re-matches #"(?s)\s*help  Show help\n.*"
                    (with-out-str
                      (tasks/help {})))
        "starts with the help target")

    (is (< 10 (count
               (str/split-lines
                (with-out-str
                  (tasks/help {})))))
        "has many lines")))

(deftest clean-test
  (testing "clean removes the target directory"
    (let [path (fs/path dir "target")]
      (fs/create-dirs path)
      (is (fs/exists? path))
      (is (= {:dir (str dir)} (tasks/clean {:dir (str dir)})) "runs")
      (is (not (fs/exists? path))))))

(deftest jar-test
  (testing "jar creates a jar file containing the sources and resources"
    (tasks/clean {:dir (str dir)})
    (let [jar-path (fs/path "target" "default-0.1.jar")
          path     (fs/path dir jar-path)]
      (is (= {:name     'mj.test/default
              :version  "0.1"
              :dir      (str dir)
              :jar-file (str path)}
             (tasks/jar {:dir (str dir)}))
          "runs")

      (is (fs/exists? path))
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
    (tasks/clean {:dir (str dir)})
    (let [classes-path (fs/path dir "target" "classes")]
      (is (= {:dir       (str dir)
              :class-dir (str classes-path)}
             (tasks/compile-clj {:dir (str dir)}))
          "runs")

      (is (fs/exists? classes-path))
      (is (fs/exists?
           (fs/path classes-path "mj" "test" "default__init.class"))))))

(deftest uber-test
  (testing "compile-clj compiles the sources files into target/classes"
    (tasks/clean {:dir (str dir)})
    (let [classes-path (fs/path dir "target" "classes")
          jar-path     (fs/path "target" "default-0.1.jar")
          path         (fs/path dir jar-path)]
      (tasks/compile-clj {:dir (str dir)})
      (is (= {:name     'mj.test/default
              :version  "0.1"
              :dir      (str dir)
              :jar-file (str path)}
             (tasks/uber {:dir (str dir)}))
          "runs")

      (is (fs/exists? path))
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
