(ns makejack.file-info.namespace-deps-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]
   [makejack.file-info.namespace-deps :as namespace-deps]))

(def form '(ns a.b.c
             (:refer-clojure :exclude [me])
             (:import a.b.c [d e] [f.g h])
             (:require [a :as aa] b [c d [e :as f]])))

(deftest parse-form-test
  (is (= '{:namespace a.b.c
           :imports   #{a.b.c d.e f.g.h},
           :requires  #{a b c.d c.e }}
         (namespace-deps/parse-form form))))

(deftest dependencies-test
  (testing "dependencies returns a map of namespace dependencies"
    (is (= '{:namespace abc
             :imports   #{a.b.c d.e f.g.h},
             :requires  #{a b c.d c.e }}
           (namespace-deps/dependencies (io/resource "abc.clj"))))))
