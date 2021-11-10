(ns makejack.namespace.api-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing]]
   [makejack.namespace.api :as namespace]))

(def form '(ns a.b.c
             (:refer-clojure :exclude [me])
             (:import a.b.c [d e] [f.g h])
             (:require
              [a :as aa]
              b
              [c
               d
               [e :as f]]
              [g :refer :all]
              [h :refer [i j]])))

(deftest parse-form-test
  (testing "parse-form returns a map of ns info"
    (is (= '{:namespace a.b.c
             :import    #{a.b.c d.e f.g.h},
             :require   #{{:namespace a :as aa}
                          {:namespace b}
                          {:namespace c.d}
                          {:namespace c.e :as f}
                          {:namespace g :refer :all}
                          {:namespace h :refer [i j]}}}
           (namespace/parse form)))))

(deftest declared-ns-test
  (testing "decalred-ns returns the namespace declared by a ns form"
    (is (= 'a.b.c
           (namespace/declared-ns form)))))

(deftest ns-form-test
  (testing "ns-form returns the ns form from a path"
    (is (= '(ns abc
              (:refer-clojure :exclude [me])
              (:import a.b.c [d e] [f.g h])
              (:require
               [a :as aa]
               b
               [c d [e :as f]]
               [g :refer :all]
               [h :refer [i j]]))
           (-> "abc.clj"
               io/resource
               namespace/ns-form)))))


(deftest dependencies-test
  (testing "dependencies returns a map of namespace dependencies"
    (is (= '{:namespace abc
             :import    #{a.b.c d.e f.g.h},
             :require   #{{:namespace a :as aa}
                          {:namespace b}
                          {:namespace c.d}
                          {:namespace c.e :as f}
                          {:namespace g :refer :all}
                          {:namespace h :refer [i j]}}}
           (-> "abc.clj"
               io/resource
               namespace/ns-form
               namespace/parse)))))
