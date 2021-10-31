(ns makejack.dag.api-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [makejack.dag.api :as dag]))

(deftest api-test
  (is (dag/graph))
  (testing "add-nodes adds child and parent dependencies"
    (let [g (dag/add-edge (dag/graph) :a :b)]
      (is (= #{:b} (dag/children g :a)))
      (is (= #{:b} (dag/transitive-children g :a)))
      (is (= #{:a} (dag/parents g :b)))
      (is (= #{:a} (dag/transitive-parents g :b)))))
  (testing "add-nodes accumulates child and parent dependencies"
    (let [g (-> (dag/graph)
                (dag/add-edge :a :b)
                (dag/add-edge :a :c))]
      (is (= #{:b :c} (dag/children g :a)))
      (is (= #{:b :c} (dag/transitive-children g :a)))
      (is (= #{:a} (dag/parents g :b)))
      (is (= #{:a} (dag/parents g :c)))
      (is (= #{:a} (dag/transitive-parents g :b)))
      (is (= #{:a} (dag/transitive-parents g :c)))))
  (testing "transitive dependencies are maintained"
    (let [g (-> (dag/graph)
                (dag/add-edge :a :b)
                (dag/add-edge :a :c)
                (dag/add-edge :c :d))]
      (is (= #{:b :c} (dag/children g :a)))
      (is (= #{:d} (dag/children g :c)))
      (is (= #{:b :c :d} (dag/transitive-children g :a)))
      (is (= #{:a} (dag/parents g :b)))
      (is (= #{:a} (dag/parents g :c)))
      (is (= #{:c} (dag/parents g :d)))
      (is (= #{:a} (dag/transitive-parents g :b)))
      (is (= #{:a} (dag/transitive-parents g :c)))
      (is (= #{:a :c} (dag/transitive-parents g :d))))))
