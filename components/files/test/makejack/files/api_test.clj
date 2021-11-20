(ns makejack.files.api-test
  (:require
   [babashka.fs :as fs]
   [clojure.test :as test :refer [deftest is testing]]
   [makejack.files.api :as files]
   [makejack.poly.api :as poly]))

(def ws (poly/workspace {:keys [:ws-dir]}))

(def dir (fs/path
          (:ws-dir ws)
          "components"
          "files"))

(deftest info-map-test
  (testing "namespace topo sort"
    (testing "with two files"
      (let [infos (files/info-map
                   {:dir dir}
                   [(fs/path "src")])]
        (is infos)
        (is (= (set '[makejack.files.api])
               (set (files/top-level-nses infos))))
        (is (= '[makejack.files.api makejack.files.impl]
               (files/topo-namespaces infos))))))
  (testing "with multiple files"
    (let [infos (files/info-map
                 {:dir (:ws-dir ws)}
                 [(fs/path "components/files/src")
                  (fs/path "components/file-info/src")
                  (fs/path "components/namespace/src")
                  (fs/path "components/dag/src")])]
      (is infos)
      (is (= (set '[makejack.files.api])
             (set (files/top-level-nses infos))))
      (is (= '[makejack.files.api
               makejack.files.impl
               makejack.file-info.api
               makejack.dag.api
               makejack.file-info.impl
               makejack.dag.impl
               makejack.namespace.api
               makejack.namespace.impl]
             (files/topo-namespaces infos))))))
