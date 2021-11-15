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
  (let [infos (files/info-map
               {:dir dir}
               [(fs/path "src")])]
    (is infos)
    (is (= (set '[makejack.files.impl makejack.files.api])
           (set (files/top-level-nses infos))))
    (is (= (set '[makejack.files.impl makejack.files.api])
           (set (files/topo-namespaces infos))))))
