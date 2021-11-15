(ns makejack.file-info.api-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]
   [makejack.file-info.api :as file-info])
  (:import
   [java.io File]))


(deftest file-info-test
  (testing "file-info"
    (let [m (file-info/file-info
             (io/resource "makejack/file_info/api_test.clj"))]
      (testing "has a file-modified time"
        (is (:last-modified m)))
      (testing "has a hash"
        (is (:file-hash m)))
      (testing "has a namespace"
        (is (= 'makejack.file-info.api-test (:namespace m))))
      (testing "has requires"
        (is (= '#{{:namespace makejack.file-info.api :as file-info}
                  {:namespace clojure.java.io :as io}
                  {:namespace clojure.test :refer [deftest is testing]}}
               (:require m))))
      (testing "has imports"
        (is (= '#{java.io.File} (:import m)))))))
