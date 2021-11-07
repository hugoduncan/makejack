(ns makejack.filesystem.api-test
  (:require
   [babashka.fs :as fs]
   [clojure.test :as test :refer [deftest is]]
   [makejack.filesystem.api :as filesystem])
  (:import
   [java.nio.file
    Files];
   [java.nio.file.attribute
    FileAttribute]))

(deftest wirh-temp-dir-test
  (let [capture-dir (volatile! nil)]
    (filesystem/with-temp-dir [dir "wirh-temp-dir-test"]
      (vreset! capture-dir dir)
      (fs/exists? dir)
      (Files/createFile
       (fs/path dir "xx")
       (into-array FileAttribute []))
      (is (fs/exists? (fs/path dir "xx"))))
    (is (not (fs/exists? (fs/path @capture-dir "xx"))))
    (is (not (fs/exists? @capture-dir)))))
