(ns association-facts-kotoba-parity-test
  "The BUSINESSNZ catalog in .cljc and in .kotoba, field by field.

  Both are readings of the same `data/datascript-tx.edn`, so this is not two
  implementations of a rule; it is one body of facts written twice, and the
  risk is transcription -- a wrong URL, a dropped field, a topic that lost its
  entry. Every field of every entry is compared, plus the counts and topic
  membership, because a catalog is exactly the shape where checking a sample
  checks the entries someone already looked at.

  `:association-rule/topic` is a SET. A set has no order and `topic` is indexed
  by position, so the port chose the order the data file writes; the assertion
  below compares against that written order rather than against `seq` on a set,
  which is not stable to rely on."
  (:require [clojure.test :refer [deftest is testing]]
            [association.facts :as facts]
            [kotoba.compiler.core :as compiler]
            [kotoba.kir :as ir]))

(def ^:private source (slurp "src/association_facts.kotoba"))
(def ^:private kir (:kir (compiler/compile-source source :js-kotoba-v1)))
(defn- call [f & args] (ir/execute kir f (vec args)))
(defn- present [option] (when (second option) (nth option 2)))

(def ^:private slug "businessnz")
(def ^:private fields
  ["id" "title" "association" "isic" "country" "kind" "url" "url-provenance"
   "source-article" "source-quote" "established-date" "last-revised-date"
   "date-unknown-because" "retrieved-at"])
(def ^:private kw->field
  {"id" :association-rule/id "title" :association-rule/title
   "association" :association-rule/association "isic" :association-rule/isic
   "country" :association-rule/country "kind" :association-rule/kind
   "url" :association-rule/url "url-provenance" :association-rule/url-provenance
   "source-article" :association-rule/source-article
   "source-quote" :association-rule/source-quote
   "established-date" :association-rule/established-date
   "last-revised-date" :association-rule/last-revised-date
   "date-unknown-because" :association-rule/date-unknown-because
   "retrieved-at" :association-rule/retrieved-at})
(def ^:private entries (vec (facts/spec-basis slug)))
(def ^:private topic-order
  "Written out on purpose. Deriving this from `entries` would compare the cljc
   against itself and assert nothing about the port."
  [["governance"] ["governance"] ["governance"] ["governance"] ["governance"]
   ["membership"] ["membership"] ["membership"] ["membership"] ["membership"]
   ["international"] ["statistics"] ["statistics"]])

(deftest the-fixture-reads-a-real-catalog
  ;; An empty catalog compares equal to an empty port.
  (is (pos? (count entries)))
  (is (= (count entries) (count topic-order))))

(deftest every-field-of-every-entry-is-transcribed
  (is (= (count entries) (call 'entry-count slug)))
  (doseq [[i entry] (map-indexed vector entries)]
    (doseq [f fields]
      (testing (str "entry " i " / " f)
        (let [expected (get entry (kw->field f))
              expected (cond (keyword? expected) (name expected)
                             (nil? expected) nil
                             :else expected)]
          (is (= expected (present (call 'entry-field slug i f)))))))))

(deftest topics-are-complete-and-in-the-order-the-port-chose
  (doseq [[i names] (map-indexed vector topic-order)]
    (testing (str "entry " i)
      (is (= (count names) (call 'topic-count slug i))
          "one number for every entry is the mistake this invites")
      (is (= (set names)
             (set (map name (:association-rule/topic (nth entries i)))))
          "the written order must name exactly the set the cljc holds")
      (doseq [[t nm] (map-indexed vector names)]
        (is (= nm (present (call 'topic slug i t))))))))

(deftest by-topic-answers-the-same-entries
  ;; Every position, not just the first. With one entry per topic the two are
  ;; the same assertion; with five they are not, and checking only index 0
  ;; would pass a port that dropped entries 1..4 of every topic.
  (doseq [t (distinct (mapcat identity topic-order))]
    (testing t
      (let [cljc (mapv :association-rule/id (facts/by-topic slug (keyword t)))]
        (is (pos? (count cljc)) "a topic nobody has makes the loop below vacuous")
        (is (= (count cljc) (call 'by-topic-count slug t)))
        (doseq [[pos id] (map-indexed vector cljc)]
          (is (= id (present (call 'by-topic-id slug t pos)))))
        (is (nil? (present (call 'by-topic-id slug t (count cljc))))
            "and it must run out exactly where the cljc does"))))
  (is (zero? (call 'by-topic-count slug "no-such-topic")))
  (is (nil? (present (call 'by-topic-id slug "no-such-topic" 0)))))

(deftest an-unknown-association-is-covered-by-nothing
  (doseq [other ["zzz" ""]]
    (is (false? (call 'association-covered? other)))
    (is (zero? (call 'entry-count other)))
    (is (nil? (present (call 'entry-field other 0 "id"))))
    (is (nil? (present (call 'coverage-note other))))
    (is (nil? (facts/spec-basis other)) "and the cljc agrees")))

(deftest the-module-compiles-for-every-target-it-claims
  (doseq [target [:js-kotoba-v1 :wasm32-kotoba-v1 :x86_64-kotoba-v1 :aarch64-kotoba-v1]]
    (testing (name target)
      (is (some? (compiler/compile-source source target {}))))))
