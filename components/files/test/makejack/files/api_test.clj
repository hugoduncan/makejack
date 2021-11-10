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
    (is (= '[makejack.files.impl makejack.files.api]
           (files/top-level-nses infos)))
    (is (= '[makejack.files.impl makejack.files.api]
           (files/topo-namespaces infos)))))
