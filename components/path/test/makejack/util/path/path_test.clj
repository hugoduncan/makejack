(ns makejack.util.path.path-test
  (:require
   [clojure.test :as test :refer [deftest is]]
   [makejack.path.api :as path])
  (:import
   [java.io
    File]))

(deftest path-test
  (is (path/path? (path/path ".")))
  (is (path/path? (path/path (File. ".")))))

(deftest path-fot-test
  (is (= (path/path "ab") (path/path-for "ab")))
  (is (= (path/path "ab" "cd") (path/path-for "ab" "cd")))
  (is (= (path/path "cd") (path/path-for nil "cd"))))

(deftest filename-test
  (is (= (path/path "fn") (path/filename (path/path "a/b/fn"))))
  (is (path/path? (path/filename (path/path "a/b/fn")))))

(deftest path-with-extension-test
  (is (= (path/path "a/b/fn.abc") (path/path-with-extension "a/b/fn" ".abc")))
  (is (= (path/path "fn.abc") (path/path-with-extension "fn" ".abc"))))
