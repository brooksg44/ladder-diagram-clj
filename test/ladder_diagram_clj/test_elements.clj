(ns ladder-diagram-clj.test-elements
  "Tests for ladder diagram elements"
  (:require [clojure.test :refer [deftest is testing]]
            [ladder-diagram-clj.elements :as elem]))

(deftest test-contact-creation
  (testing "Contact element creation"
    (let [contact (elem/contact "TestContact")]
      (is (= (:name contact) "TestContact"))
      (is (= (:ascii-art contact) "─┤ ├─"))
      (is (= (:justify contact) :center)))))

(deftest test-negated-contact-creation
  (testing "Negated contact element creation"
    (let [neg-contact (elem/negated-contact "TestNegContact")]
      (is (= (:name neg-contact) "TestNegContact"))
      (is (= (:ascii-art neg-contact) "─┤/├─"))
      (is (= (:justify neg-contact) :center)))))

(deftest test-coil-creation
  (testing "Coil element creation"
    (let [coil (elem/coil "TestCoil")]
      (is (= (:name coil) "TestCoil"))
      (is (= (:ascii-art coil) "─( )"))
      (is (= (:justify coil) :right)))))

(deftest test-element-width-calculation
  (testing "Element width calculation"
    (let [short-name-contact (elem/contact "A")
          long-name-contact (elem/contact "VeryLongContactName")]
      (is (= (elem/element-width short-name-contact) 5))  ; ASCII art length
      (is (= (elem/element-width long-name-contact) 19))))) ; Name length

(deftest test-element-rendering
  (testing "Element ASCII rendering"
    (let [contact (elem/contact "Test")]
      (is (= (elem/render-ascii contact) "─┤ ├─"))
      (is (= (elem/render-name contact) "Test")))))

(deftest test-element-type-predicates
  (testing "Element type predicates"
    (let [contact (elem/contact "Test")
          coil (elem/coil "Test")
          neg-contact (elem/negated-contact "Test")]
      (is (elem/contact? contact))
      (is (not (elem/contact? coil)))
      (is (elem/coil? coil))
      (is (not (elem/coil? contact)))
      (is (elem/negated? neg-contact))
      (is (not (elem/negated? contact))))))

(deftest test-element-name-update
  (testing "Element name updating"
    (let [contact (elem/contact "OldName")
          updated (elem/update-element-name contact "NewName")]
      (is (= (:name updated) "NewName"))
      (is (= (:ascii-art updated) (:ascii-art contact))))))

(deftest test-element-type-name
  (testing "Element type name identification"
    (is (= (elem/element-type-name (elem/contact)) "Contact"))
    (is (= (elem/element-type-name (elem/negated-contact)) "Negated Contact"))
    (is (= (elem/element-type-name (elem/coil)) "Coil"))
    (is (= (elem/element-type-name (elem/negated-coil)) "Negated Coil"))
    (is (= (elem/element-type-name (elem/set-coil)) "Set Coil"))
    (is (= (elem/element-type-name (elem/reset-coil)) "Reset Coil"))
    (is (= (elem/element-type-name (elem/toggle-coil)) "Toggle Coil"))))

(deftest test-all-element-types
  (testing "All element types can be created"
    (let [elements [(elem/contact "Test")
                    (elem/negated-contact "Test")
                    (elem/rising-edge-contact "Test")
                    (elem/falling-edge-contact "Test")
                    (elem/edge-contact "Test")
                    (elem/coil "Test")
                    (elem/negated-coil "Test")
                    (elem/set-coil "Test")
                    (elem/reset-coil "Test")
                    (elem/toggle-coil "Test")]]
      (is (every? #(satisfies? elem/Renderable %) elements))
      (is (every? #(satisfies? elem/Measurable %) elements))
      (is (every? #(= (elem/element-depth %) 2) elements)))))

(deftest test-justify-string
  (testing "String justification"
    (is (= (elem/justify-string "test" 8 :center) "──test──"))
    (is (= (elem/justify-string "test" 8 :left) "test────"))
    (is (= (elem/justify-string "test" 8 :right) "────test"))))