(ns association.facts-test
  (:require [clojure.test :refer [deftest is]]
            [association.facts :as facts]))

(deftest businessnz-has-spec-basis
  (let [sb (facts/spec-basis "businessnz")]
    (is (= 2 (count sb)))
    (is (every? #(= "9411" (:association-rule/isic %)) sb))
    (is (every? #(= "NZL" (:association-rule/country %)) sb))))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "ibec")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["businessnz" "ibec"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ibec"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= 2 (count (facts/by-topic "businessnz" :governance))))
  (is (empty? (facts/by-topic "businessnz" :labor)))
  (is (empty? (facts/by-topic "ibec" :governance))))
