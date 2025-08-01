(ns ladder-diagram-clj.core-test
  (:require [clojure.test :refer :all]
            [ladder-diagram-clj.elements :as elem]
            [ladder-diagram-clj.ladder :as ladder]))

(deftest test-core-functionality
  (testing "Core ladder creation and rendering functions work"
    (let [simple-ladder (ladder/ladder
                         (ladder/rung
                          (elem/contact "In1")
                          (elem/coil "Out1")))
          rendered (elem/render-ascii simple-ladder)]
      (is (string? rendered))
      (is (clojure.string/includes? rendered "In1"))
      (is (clojure.string/includes? rendered "Out1")))))
