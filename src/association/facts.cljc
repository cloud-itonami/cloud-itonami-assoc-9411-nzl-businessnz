(ns association.facts
  "Industry rule/history catalog for Business New Zealand (BusinessNZ)
  -- a 46th industry-association-level source (see
  cloud-itonami-assoc-9411-sau-fsc, -9411-aut-wko, -9411-irl-ibec for
  the first three) per ADR-2607141700
  (cloud-itonami-compliance-fact-federation). The FOURTH entry
  aligned to ISIC 9411 (activities of business, employers, and
  professional membership organizations). Fills New Zealand's
  previously-open association-axis gap (noted honestly at tick 136)
  -- New Zealand now has real, individually verified facts across
  ALL THREE axes (municipality:
  cloud-itonami-municipality-nzl-wellington, tick 134; country:
  cloud-itonami-iso3166-nzl statute.facts, tick 136; association:
  this entry, tick 137).

  businessnz.org.nz and teara.govt.nz (Te Ara Encyclopedia of New
  Zealand, an official government-run resource) both returned HTTP
  403 on every URL tried, so both entries here were directly
  WebFetch-verified against en.wikipedia.org's own article: 'In 1902
  several regional employers' associations came together to form the
  New Zealand Employers Federation... In 2001 the New Zealand
  Employers Federation merged with the New Zealand Manufacturers
  Federation to form Business New Zealand (BusinessNZ)' (the current
  chief executive's name incidentally encountered but never persisted
  here).

  An association not in `catalog` has NO spec-basis, full stop; never
  fabricate one.")

(def catalog
  "association-slug -> vector of association-rule entries."
  {"businessnz"
   [{:association-rule/id "businessnz.predecessor-employers-federation-1902"
     :association-rule/title "New Zealand Employers Federation founded, BusinessNZ's earliest predecessor body (Wikipedia)"
     :association-rule/association "businessnz"
     :association-rule/isic "9411"
     :association-rule/country "NZL"
     :association-rule/kind :governance-program
     :association-rule/url "https://en.wikipedia.org/wiki/Business_New_Zealand"
     :association-rule/url-provenance :wikipedia-corroborated
     :association-rule/established-date "1902"
     :association-rule/retrieved-at "2026-07-17"
     :association-rule/topic #{:governance}}
    {:association-rule/id "businessnz.founding-2001-merger"
     :association-rule/title "BusinessNZ founded via merger of NZ Employers Federation and NZ Manufacturers Federation (Wikipedia)"
     :association-rule/association "businessnz"
     :association-rule/isic "9411"
     :association-rule/country "NZL"
     :association-rule/kind :governance-program
     :association-rule/url "https://en.wikipedia.org/wiki/Business_New_Zealand"
     :association-rule/url-provenance :wikipedia-corroborated
     :association-rule/established-date "2001"
     :association-rule/retrieved-at "2026-07-17"
     :association-rule/topic #{:governance}}]})

(defn spec-basis [association] (get catalog association))

(defn coverage
  ([] (coverage (keys catalog)))
  ([associations]
   (let [have (filter catalog associations)
         missing (remove catalog associations)]
     {:requested (count associations)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-9411-nzl-businessnz Wave 0 (ADR-2607141700): "
                 (count (get catalog "businessnz")) " BusinessNZ entries seeded "
                 "with Wikipedia citations (businessnz.org.nz/teara.govt.nz both 403'd). "
                 "Extend `association.facts/catalog`, never fabricate an id/url.")})))

(defn by-topic [association topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis association)))
