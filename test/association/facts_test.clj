(ns association.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [association.facts :as facts]))

(def ^:private authored
  "data/datascript-tx.edn is the AUTHORED catalog; this namespace holds the same
   entries inline. Reading the file here is what stops the two copies drifting."
  (edn/read-string (slurp "data/datascript-tx.edn")))

(def ^:private entries (vec (facts/spec-basis "businessnz")))

(deftest the-fixture-reads-a-real-catalog
  ;; An empty catalog satisfies every `every?` below. Without this floor,
  ;; deleting the catalog would make this suite greener, not redder.
  (is (pos? (count entries)))
  (is (pos? (count authored))))

(deftest cljc-holds-exactly-what-the-data-file-authors
  (is (= (count authored) (count entries))
      "an entry added to one copy and not the other is the drift this catches")
  (doseq [[i [a e]] (map-indexed vector (map vector authored entries))]
    (testing (str "entry " i " (" (:association-rule/id a) ")")
      ;; :topic is a vector in the data file and a set in the cljc, so it is
      ;; compared as a set; every other field must be identical.
      (is (= (dissoc a :association-rule/topic) (dissoc e :association-rule/topic)))
      (is (= (set (:association-rule/topic a)) (:association-rule/topic e))))))

(deftest businessnz-has-spec-basis
  (is (= 13 (count entries)))
  (is (every? #(= "9411" (:association-rule/isic %)) entries))
  (is (every? #(= "NZL" (:association-rule/country %)) entries))
  (is (every? #(str/starts-with? (:association-rule/id %) "businessnz.") entries)))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "ibec")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["businessnz" "ibec"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ibec"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= 5 (count (facts/by-topic "businessnz" :governance))))
  (is (= 5 (count (facts/by-topic "businessnz" :membership))))
  (is (= 2 (count (facts/by-topic "businessnz" :statistics))))
  (is (= 1 (count (facts/by-topic "businessnz" :international))))
  (is (empty? (facts/by-topic "businessnz" :labor)))
  (is (empty? (facts/by-topic "ibec" :governance)))
  (is (= (count entries)
         (reduce + (map #(count (facts/by-topic "businessnz" %))
                        [:governance :membership :statistics :international])))
      "every entry must be reachable by some topic, or by-topic hides it"))

(deftest every-entry-can-be-checked-against-its-own-source
  ;; The point of :source-quote is that a citation can be re-checked. An entry
  ;; without one is not wrong -- it is unfalsifiable, which is worse, because
  ;; it reads exactly like a verified one.
  (is (facts/every-entry-is-checkable? "businessnz"))
  (is (every? #(seq (:association-rule/source-article %)) entries))
  (is (not (facts/every-entry-is-checkable? "zzz"))
      "an association with no entries is not vacuously checkable"))

(deftest the-catalog-rests-on-more-than-one-source
  ;; This repo began as two entries citing one URL. Counting entries would have
  ;; called that catalog twice as strong as it was; counting sources does not.
  (let [srcs (facts/sources "businessnz")]
    (is (= 7 (count srcs)))
    (is (every? #(str/starts-with? % "https://") srcs))
    (is (empty? (facts/sources "zzz")))))

(deftest a-bare-origin-url-is-never-cited
  ;; businessnz.org.nz answers 403 to every client tried, so an entry citing it
  ;; directly could never be re-verified -- and would fail silently, by looking
  ;; like every other entry.
  (doseq [e entries]
    (let [u (:association-rule/url e)
          p (name (:association-rule/url-provenance e))]
      (testing (:association-rule/id e)
        (when (str/includes? u "businessnz.org.nz")
          (is (str/starts-with? u "https://web.archive.org/web/")
              "an unfetchable citation must not be recorded as a source")
          (is (str/ends-with? p "-web-archive")
              "and the provenance must say how the words were actually obtained"))))))

(deftest no-entry-persists-a-personal-name
  ;; BusinessNZ publishes its board members and staff by name; organization.edn
  ;; states that this repo records institutional facts only. The board page a
  ;; quote below is read from lists names one sentence later.
  (doseq [e entries]
    (testing (:association-rule/id e)
      (is (not (str/includes? (:association-rule/url e) "/our-people/"))))))
