(ns fractals.fractal-test
  (:require [clojure.test :refer :all]
            [fractals.fractal :refer :all]))

(import '(org.apfloat Apfloat ApfloatMath))

(deftest can-square-apfloat
  (testing "Square of Apfloat value"
    (is (.equals (Apfloat. 100 10) (square (Apfloat. 10 10))))))

(deftest is-constrained-test
  (testing "Verify constraint testing"
    (is (= false (is-constrained (v 2) (v 0) 10)))
    (is (= true (is-constrained (v "1.0E-7") (v "0") 100)))))

(deftest scale-pos-in-fractal-test
  (testing "Able to get scaled X position"
    (is (.equals (Apfloat. 10 10) (scale-pos-in-fractal 0.1 (Apfloat. 0 1) (Apfloat. 100 1))))))