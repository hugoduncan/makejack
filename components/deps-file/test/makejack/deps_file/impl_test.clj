(ns makejack.deps-file.impl-test
  (:require
   [clojure.test :refer [deftest is]]
   [makejack.deps-file.impl :as deps-file]))


(deftest update-in-deps-keys-test
  (is (= "{:deps {abc/def {:git/sha \"123\"}}}"
         (deps-file/update-in-deps-keys
          "{:deps {abc/def {:git/sha \"000\"}}}"
          'abc/def {:git/sha "123"})))
  (is (= (pr-str '{:deps {abc/def {:git/sha "123"}}})
         (deps-file/update-in-deps-keys
          (pr-str '{:deps {abc/def {:git/sha "000"}}})
          'abc/def {:git/sha "123"})))
  (is (= (pr-str '{:another {}
                   :deps    {abc/def {:git/sha "123"}}
                   :aliase  {:deps {abc/def {:git/sha "123"}}}})
         (deps-file/update-in-deps-keys
          (pr-str '{:another {}
                    :deps    {abc/def {:git/sha "000"}}
                    :aliase  {:deps {abc/def {:git/sha "000"}}}})
          'abc/def {:git/sha "123"}))))
