(ns makejack.deps-file.api-test
  (:require
   [babashka.fs :as fs]
   [clojure.test :refer [deftest is]]
   [makejack.deps-file.api :as deps-file]
   [makejack.filesystem.api :as filesystem]))


(deftest update-dep-test
  (filesystem/with-temp-dir [dir {:prefix "update-dep-test"}]
    (let [f (fs/file dir "deps.edn")]
      (spit f (pr-str '{:deps {abc/def {:mvn/version "0.0.0"}}}))
      (deps-file/update-dep
       {:dir           dir
        :artifact-name 'abc/def
        :mvn/version   "0.1.0"})
      (is (=
           (pr-str '{:deps {abc/def {:mvn/version "0.1.0"}}})
           (slurp f))))))
