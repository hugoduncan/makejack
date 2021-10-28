(ns makejack.project-data.impl-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [makejack.project-data.impl :as impl]))

(deftest version-map-test
  (testing "version-map->version and version->version-map round trip"
    (doseq [v ["0" "0.1" "0.1.1" "0-q" "0.1-q" "0.1.1-q"]]
      (is (= v (impl/version-map->version (impl/version->version-map v)))
          (str " with version " v)))))
