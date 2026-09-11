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

## Scope

A **read-only reference/archive** catalog — not an Advisor⊣Governor
actuation actor. It proposes or executes nothing on BusinessNZ's
behalf.

Coverage is reported honestly (see `association.facts/coverage`): an
association not in `catalog` has **no spec-basis**, full stop — never
fabricate one.

The catalog records **institutional facts only**. BusinessNZ publishes
the names of its board members and staff; no entry is read from
`/our-people/`, and no quote spans a personal name — even where the
surrounding sentence was the most convenient one to cite.

## Sourcing note

`businessnz.org.nz` and `teara.govt.nz` (Te Ara Encyclopedia of New
Zealand, an official government-run resource) answer **HTTP 403 to
every client this workspace has tried, on every URL tried** — measured
2026-07-17 and measured again 2026-09-06. That is a standing property
of those hosts, not a bad afternoon.

So the association's own words are cited through **Internet Archive
captures**, whose URLs carry the capture instant and stay fetchable.
This is recorded as `:official-businessnz-org-nz-web-archive` rather
than smuggled in as a direct read — and `verify-catalog.cljs` rejects
an entry whose URL and provenance disagree about which of the two it
is, in either direction.

The two founding entries (1902, 2001) rest on `en.wikipedia.org` and
say so.

## Verifying the catalog

```bash
kbb --backend sci scripts/verify-catalog.cljk           # structural only (offline)
kbb --backend sci scripts/verify-catalog.cljk --live    # fetch every source, check every quote
```

Every entry names the page it comes from (`:source-article`) and the
**verbatim span it rests on** (`:source-quote`). `--live` does not ask
whether a citation resolves; it asks whether the document *still says
the thing the entry says it says*. Reachability alone would not do: a
URL that answers 200 without the claim looks exactly like one that
carries it.

Exit codes are three-valued on purpose — `0` checked and clean, `1`
checked and findings printed, `2` **refused**, because "I could not
read the catalog" and "I read it and it was fine" must not leave the
same trace.

Where a page states no adoption date — a membership term or a board
composition is published undated — the entry carries
`:date-unknown-because` rather than a date invented from the capture
instant. A forgotten date and a recorded absence must not look the
same.

## Data

- `data/datascript-tx.edn` — **the authored catalog**, source of truth.
- `src/association/facts.cljc` — the same entries inline, held to the
  data file by `test/association/facts_test.clj`.
- `src/association_facts.kotoba` — **generated** from the data file by
  `kbb --backend sci scripts/gen-kotoba-port.cljk` (`--check` fails if someone hand-edits
  it). This copy reaches the Kotoba oracle, wasm and both native ISAs,
  which the `.cljc` cannot.
- `schema/association-rule.edn` — DataScript schema.

Query alongside other `cloud-itonami`/`etzhayyim` compliance-fact
sources via `com-junkawasaki/root`'s `scripts/compliance-fact-query.cljs`.

`test/association_facts_kotoba_parity_test.clj` compares every field of
every entry across the two copies, plus counts and topic membership,
and compiles the port for all four targets it claims.

## License

AGPL-3.0-or-later (matches the `cloud-itonami-iso3166-*` /
`-municipality-*` / `-assoc-*` / `-lei-*` convention). Policy text
itself remains BusinessNZ's; this repo stores only citation metadata
(id/title/url/dates) and short verbatim spans quoted for verification,
not full text.
