# cloud-itonami-assoc-9411-nzl-businessnz

Industry rule/history catalog for **Business New Zealand**
(BusinessNZ) — the FOURTH entry aligned to **ISIC 9411** (activities
of business, employers, and professional membership organizations),
alongside
[`-9411-sau-fsc`](https://github.com/cloud-itonami/cloud-itonami-assoc-9411-sau-fsc)
(Saudi Arabia),
[`-9411-aut-wko`](https://github.com/cloud-itonami/cloud-itonami-assoc-9411-aut-wko)
(Austria), and
[`-9411-irl-ibec`](https://github.com/cloud-itonami/cloud-itonami-assoc-9411-irl-ibec)
(Ireland). Part of the
[`cloud-itonami`](https://github.com/cloud-itonami) compliance-fact
family (ADR-2607141700, `cloud-itonami-compliance-fact-federation`,
in `com-junkawasaki/root`).

## Sourcing note

This repo fills New Zealand's previously-open association-axis gap
(noted honestly at tick 136). New Zealand now has real, individually
verified facts across all three axes: municipality
([`cloud-itonami-municipality-nzl-wellington`](https://github.com/cloud-itonami/cloud-itonami-municipality-nzl-wellington)),
country
([`cloud-itonami-iso3166-nzl`](https://github.com/cloud-itonami/cloud-itonami-iso3166-nzl)),
and association (this repo).

`businessnz.org.nz` and `teara.govt.nz` (Te Ara Encyclopedia of New
Zealand, an official government-run resource) both returned HTTP 403
on every URL tried — both entries here were instead directly
confirmed via `en.wikipedia.org`.

## Scope

A **read-only reference/archive** catalog — not an Advisor⊣Governor
actuation actor. It proposes or executes nothing on BusinessNZ's
behalf.

Coverage is reported honestly (see `association.facts/coverage`): an
association not in `catalog` has **no spec-basis**, full stop — never
fabricate one.

## Data

- `src/association/facts.cljc` — the catalog, source of truth.
- `schema/association-rule.edn` — DataScript schema.
- `data/datascript-tx.edn` — derived DataScript tx-data (query this
  alongside other `cloud-itonami`/`etzhayyim` compliance-fact sources via
  `com-junkawasaki/root`'s `scripts/compliance-fact-query.cljs`).

Both entries directly WebFetch-verified against `en.wikipedia.org`'s
own article: the 1902 founding of the New Zealand Employers Federation
(BusinessNZ's earliest predecessor body) and the 2001 merger with the
New Zealand Manufacturers Federation that formed BusinessNZ.

## License

AGPL-3.0-or-later (matches the `cloud-itonami-iso3166-*` /
`-municipality-*` / `-assoc-*` / `-lei-*` convention). Policy text
itself remains BusinessNZ's; this repo stores only citation metadata
(id/title/url/dates), not full text.
