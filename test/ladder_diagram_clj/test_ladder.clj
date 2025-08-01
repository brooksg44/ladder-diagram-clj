(ns ladder-diagram-clj.test-ladder
  "Tests for ladder diagram core functionality"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [ladder-diagram-clj.elements :as elem]
            [ladder-diagram-clj.ladder :as ladder]))

(deftest test-rung-creation
  (testing "Rung creation and basic properties"
    (let [rung (ladder/rung (elem/contact "In1") (elem/coil "Out1"))]
      (is (instance? ladder_diagram_clj.ladder.Rung rung))
      (is (= (count (:elements rung)) 2))
      (is (= (elem/element-depth rung) 2)))))

(deftest test-branch-creation
  (testing "Branch creation and basic properties"
    (let [rung1 (ladder/rung (elem/contact "In1"))
          rung2 (ladder/rung (elem/contact "In2"))
          branch (ladder/branch rung1 rung2)]
      (is (instance? ladder_diagram_clj.ladder.Branch branch))
      (is (= (count (:rungs branch)) 2)))))

(deftest test-ladder-creation
  (testing "Ladder creation and basic properties"
    (let [rung1 (ladder/rung (elem/contact "In1") (elem/coil "Out1"))
          rung2 (ladder/rung (elem/contact "In2") (elem/coil "Out2"))
          ladder-diagram (ladder/ladder rung1 rung2)]
      (is (instance? ladder_diagram_clj.ladder.Ladder ladder-diagram))
      (is (= (count (:rungs ladder-diagram)) 2)))))

(deftest test-simple-ladder-ascii-rendering
  (testing "Simple ladder ASCII rendering matches expected output"
    (let [ladder-diagram (ladder/ladder
                          (ladder/rung
                           (elem/contact)
                           (elem/coil)))]
      (let [rendered (elem/render-ascii ladder-diagram)]
        (is (str/includes? rendered "█"))
        (is (str/includes? rendered "┤ ├"))
        (is (str/includes? rendered "( )"))))))

(deftest test-named-elements-ladder
  (testing "Ladder with named elements"
    (let [ladder-diagram (ladder/ladder
                          (ladder/rung
                           (elem/contact "In1")
                           (elem/contact "In2")
                           (elem/coil "Out1")))]
      (let [rendered (elem/render-ascii ladder-diagram)]
        (is (str/includes? rendered "In1"))
        (is (str/includes? rendered "In2"))
        (is (str/includes? rendered "Out1"))))))

(deftest test-negated-contact-rendering
  (testing "Negated contact rendering"
    (let [ladder-diagram (ladder/ladder
                          (ladder/rung
                           (elem/negated-contact)
                           (elem/coil)))]
      (let [rendered (elem/render-ascii ladder-diagram)]
        (is (str/includes? rendered "┤/├"))))))

(deftest test-ladder-with-branch
  (testing "Ladder with branch rendering"
    (let [ladder-diagram (ladder/ladder
                          (ladder/branch
                           (ladder/rung
                            (elem/contact "In1")
                            (elem/contact "In2")
                            (elem/coil "Out1"))
                           (ladder/rung
                            (elem/contact "In1")
                            (elem/negated-contact "In2")
                            (elem/coil "Out2"))))]
      (let [rendered (elem/render-ascii ladder-diagram)]
        (is (str/includes? rendered "┬"))  ; Branch start
        (is (str/includes? rendered "└"))  ; Branch end
        (is (str/includes? rendered "│"))  ; Vertical connection
        ))))

(deftest test-ladder-statistics
  (testing "Ladder statistics calculation"
    (let [ladder-diagram (ladder/ladder
                          (ladder/rung
                           (elem/contact "In1")
                           (elem/coil "Out1"))
                          (ladder/rung
                           (elem/contact "In2")
                           (elem/coil "Out2")))
          stats (ladder/ladder-statistics ladder-diagram)]
      (is (= (:total-rungs stats) 2))
      (is (= (:total-elements stats) 4))
      (is (= (:total-branches stats) 0)))))

(deftest test-ladder-with-branch-statistics
  (testing "Ladder statistics with branches"
    (let [ladder-diagram (ladder/ladder
                          (ladder/branch
                           (ladder/rung (elem/contact "In1"))
                           (ladder/rung (elem/contact "In2"))))
          stats (ladder/ladder-statistics ladder-diagram)]
      (is (= (:total-rungs stats) 1))
      (is (= (:total-elements stats) 0))  ; Branch doesn't count elements directly
      (is (= (:total-branches stats) 1)))))

(deftest test-add-element-to-rung
  (testing "Adding element to rung"
    (let [rung (ladder/rung (elem/contact "In1"))
          updated-rung (ladder/add-element-to-rung rung (elem/coil "Out1"))]
      (is (= (count (:elements updated-rung)) 2))
      (is (= (:name (last (:elements updated-rung))) "Out1")))))

(deftest test-remove-element-from-rung
  (testing "Removing element from rung"
    (let [rung (ladder/rung (elem/contact "In1") (elem/contact "In2") (elem/coil "Out1"))
          updated-rung (ladder/remove-element-from-rung rung 1)]
      (is (= (count (:elements updated-rung)) 2))
      (is (= (:name (second (:elements updated-rung))) "Out1")))))

(deftest test-add-rung-to-ladder
  (testing "Adding rung to ladder"
    (let [ladder-diagram (ladder/ladder (ladder/rung (elem/contact "In1")))
          new-rung (ladder/rung (elem/contact "In2"))
          updated-ladder (ladder/add-rung-to-ladder ladder-diagram new-rung)]
      (is (= (count (:rungs updated-ladder)) 2)))))

(deftest test-get-element-at
  (testing "Getting element at specific position"
    (let [ladder-diagram (ladder/ladder
                          (ladder/rung
                           (elem/contact "In1")
                           (elem/coil "Out1")))
          element (ladder/get-element-at ladder-diagram 0 0)]
      (is (= (:name element) "In1")))))

(deftest test-update-element-at
  (testing "Updating element at specific position"
    (let [ladder-diagram (ladder/ladder
                          (ladder/rung
                           (elem/contact "In1")
                           (elem/coil "Out1")))
          new-element (elem/contact "NewIn1")
          updated-ladder (ladder/update-element-at ladder-diagram 0 0 new-element)]
      (is (= (:name (ladder/get-element-at updated-ladder 0 0)) "NewIn1")))))

(deftest test-ladder-validation
  (testing "Ladder validation"
    (let [valid-rung (ladder/rung (elem/contact "In1") (elem/coil "Out1"))
          valid-ladder (ladder/ladder valid-rung)]
      (is (ladder/valid-rung? valid-rung))
      (is (ladder/valid-ladder? valid-ladder)))))

;; Test that reproduces the Python test cases
(deftest test-python-compatibility
  (testing "Compatibility with original Python examples"
    ;; Test case from Python: simple ladder
    (let [ladder-diagram (ladder/ladder
                          (ladder/rung
                           (elem/contact)
                           (elem/coil)))
          rendered (elem/render-ascii ladder-diagram)]
      (is (str/includes? rendered "█"))
      (is (str/includes? rendered "┤ ├"))
      (is (str/includes? rendered "( )")))

    ;; Test case: negated contact
    (let [ladder-diagram (ladder/ladder
                          (ladder/rung
                           (elem/negated-contact)
                           (elem/coil)))
          rendered (elem/render-ascii ladder-diagram)]
      (is (str/includes? rendered "┤/├")))

    ;; Test case: named elements
    (let [ladder-diagram (ladder/ladder
                          (ladder/rung
                           (elem/contact "In1")
                           (elem/contact "In2")
                           (elem/coil "Out1")))
          rendered (elem/render-ascii ladder-diagram)]
      (is (str/includes? rendered "In1"))
      (is (str/includes? rendered "In2"))
      (is (str/includes? rendered "Out1")))))