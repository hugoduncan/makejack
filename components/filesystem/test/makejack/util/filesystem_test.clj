(ns makejack.util.filesystem-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :as test :refer [deftest is testing]]
   [makejack.util.filesystem :as fs]
   [makejack.util.path :as path])
  (:import
   [java.nio.file
    CopyOption
    Files
    StandardCopyOption];
   [java.nio.file.attribute
    FileAttribute]
   [java.util
    Arrays]))

(deftest make-temp-path-path-test
  (testing "with default options creates a path with tmp prefix and .tmp suffix"
    (let [path (fs/make-temp-path {})]
      (is (fs/file-exists? path))
      (is (str/starts-with? (str (path/filename path)) "tmp"))
      (is (str/ends-with? (str (path/filename path)) ".tmp"))
      (fs/delete-file! path)))
  (testing "with sxplicit options creates a path with the given prefix and suffix"
    (let [path (fs/make-temp-path {:prefix "pre" :suffix ".sfx"})]
      (is (fs/file-exists? path))
      (is (str/starts-with? (str (path/filename path)) "pre"))
      (is (str/ends-with? (str (path/filename path)) ".sfx"))
      (fs/delete-file! path)))
  (testing "with string option creates a path with the given prefix prefix"
    (let [path (fs/make-temp-path "pref")]
      (is (fs/file-exists? path))
      (is (str/starts-with? (str (path/filename path)) "pref"))
      (fs/delete-file! path)))
  (testing "with directory option creates a path in the given directory"
    (let [dir  (Files/createTempDirectory "xyz" fs/empty-file-attributes)
          path (fs/make-temp-path {:prefix "pref" :dir dir})]
      (is (fs/file-exists? path))
      (is (= dir (path/parent path)))
      (is (str/starts-with? (str (path/filename path)) "pref"))
      (fs/delete-file! path))))

(deftest with-temp-path-test
  (let [paths (volatile! [])]
    (fs/with-temp-path [path {}]
      (vreset! paths path)
      (is (fs/file-exists? path))
      (is (str/starts-with? (str (path/filename path)) "tmp"))
      (is (str/ends-with? (str (path/filename path)) ".tmp")))
    (is (not (fs/file-exists? @paths)))

    (fs/with-temp-path [path {}
                                path2 "pref"]
      (is (fs/file-exists? path))
      (is (fs/file-exists? path2))
      (vreset! paths [path path2])
      (is (str/starts-with? (str (path/filename path2)) "pref")))
    (is (not (fs/file-exists? (first @paths))))
    (is (not (fs/file-exists? (second @paths))))))

(deftest list-paths-test
  (let [source (.getPath (io/resource "project"))]
    (is (=
         ["" "sub" "sub/mj.edn" "sub/project.edn" "mj.edn" "project.edn"]
         (->> (fs/list-paths source)
              (mapv (path/relative-to source))
              (mapv str))))))

(deftest copy-files-test
  (let [file-attributes (into-array FileAttribute [])
        dir             (Files/createTempDirectory
                         "delete-recursive-test" file-attributes)
        source          (.getPath (io/resource "project"))]
    (fs/copy-files! source dir)
    (is (= (mapv (path/relative-to source) (fs/list-paths source))
           (mapv (path/relative-to dir) (fs/list-paths dir))))))

(deftest delete-recursive-test
  (let [file-attributes (into-array FileAttribute [])
        dir             (Files/createTempDirectory
                         "delete-recursive-test" file-attributes)]
    (doseq [sub-dir (range 3)]
      (fs/mkdirs (path/path dir (str sub-dir)))
      (doseq [file (range 3)]
        (Files/createFile
         (path/path dir (str sub-dir) (str file))
         (into-array FileAttribute []))))
    (fs/delete-recursively! (str dir))
    (is (not (fs/file-exists? dir))
        (str dir))))

(deftest wirh-temp-dir-test
  (let [capture-dir (volatile! nil)]
    (fs/with-temp-dir [dir "wirh-temp-dir-test"]
      (vreset! capture-dir dir)
      (fs/file-exists? dir)
      (Files/createFile
       (path/path dir "xx")
       (into-array FileAttribute []))
      (is (fs/file-exists? (path/path dir "xx"))))
    (is (not (fs/file-exists? (path/path @capture-dir "xx"))))
    (is (not (fs/file-exists? @capture-dir)))))

(deftest copy-options-test
  (is (Arrays/equals
       ^"[Ljava.nio.file.CopyOption;" (into-array CopyOption [])
       (fs/copy-options {})))
  (is (Arrays/equals
       ^"[Ljava.nio.file.CopyOption;" (into-array
                                       CopyOption
                                       [StandardCopyOption/COPY_ATTRIBUTES])
       (fs/copy-options {:copy-attributes true})))
  (is (Arrays/equals
       ^"[Ljava.nio.file.CopyOption;" (into-array
                                       CopyOption
                                       [StandardCopyOption/REPLACE_EXISTING])
       (fs/copy-options {:replace-existing true})))
  (is (Arrays/equals
       ^"[Ljava.nio.file.CopyOption;" (into-array
                                       CopyOption
                                       [StandardCopyOption/COPY_ATTRIBUTES
                                        StandardCopyOption/REPLACE_EXISTING])
       (fs/copy-options {:copy-attributes true :replace-existing true}))))
