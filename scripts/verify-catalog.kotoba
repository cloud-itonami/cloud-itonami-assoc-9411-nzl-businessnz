#!/usr/bin/env nbb
;; scripts/verify-catalog.cljs — check the BusinessNZ catalog against its own
;; sources.
;;
;;   nbb scripts/verify-catalog.cljs            structural only (offline)
;;   nbb scripts/verify-catalog.cljs --live     also fetch every :url and require
;;                                              every :source-quote to be in it
;;
;; Ported from cloud-itonami-assoc-9411-ury-ciu, plus one check this catalog
;; needs that a catalog citing live pages does not (see :archive-shape below).
;;
;; Exit codes are three-valued on purpose:
;;
;;   0  checked, nothing wrong
;;   1  checked, findings printed
;;   2  REFUSED -- could not check. Not 0, because "I could not read the
;;      catalog" and "I read the catalog and it was fine" must not leave the
;;      same trace, and not 1, because there is no finding to act on.
;;
;; Why :source-quote exists at all: reachability is not support. A URL that
;; returns HTTP 200 and does not contain the claim looks exactly like a URL
;; that does, so a citation can rot without anyone noticing. --live does not
;; ask whether the citation resolves; it asks whether the document still says
;; the thing the entry says it says.
;;
;; Why :archive-shape exists here specifically: businessnz.org.nz answers 403
;; to every client this workspace has tried, on every URL tried, in July 2026
;; and again in September 2026. The association's own words are reachable only
;; through Internet Archive captures, so this catalog's citations point at
;; web.archive.org. That is a deliberate provenance, not a shortcut, and it has
;; a failure mode of its own: an entry that records a bare businessnz.org.nz
;; URL would make --live refuse forever, and an entry that points at an archive
;; capture while claiming to be a direct read would overstate how the fact was
;; obtained. Both are silent. So the URL and the provenance keyword must agree,
;; and the check says which one it thinks is wrong.

(ns verify-catalog
  (:require [clojure.edn :as edn]
            [kotoba.lang.text :as str]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]
            ["child_process" :as cp]))

;; process.argv holds this script's own path. Dropping a fixed count gets it
;; wrong the moment the launcher changes, and the symptom is that the script
;; path becomes the catalog path -- which this script then reports as
;; unreadable, i.e. a refusal that looks like a broken catalog.
(def argv (vec (remove #(str/ends-with? % "verify-catalog.cljs")
                       (drop 2 (js->clj (.-argv js/process))))))
(def live? (some #{"--live"} argv))
(def data-path
  (or (first (remove #(str/starts-with? % "--") argv)) "data/datascript-tx.edn"))

(def ASSOCIATION "businessnz")
(def ISIC "9411")
(def COUNTRY "NZL")

;; The one host whose captures this catalog treats as the association speaking.
(def ARCHIVE-HOST "https://web.archive.org/web/")
(def ORIGIN-HOST "businessnz.org.nz")

(defn refuse! [msg]
  (println (str "REFUSED: " msg))
  (println "Refusing to report a pass on a catalog this run could not read.")
  (.exit js/process 2))

(defn- read-catalog []
  (let [txt (try (fs/readFileSync data-path "utf8")
                 (catch :default e (refuse! (str data-path ": " (.-message e)))))
        data (try (edn/read-string txt)
                  (catch :default e (refuse! (str data-path " is not readable EDN: "
                                                 (.-message e)))))]
    (when-not (vector? data)
      (refuse! (str data-path " is not a vector of entries (got "
                    (if (nil? data) "nil" (type data)) ")")))
    (when (empty? data)
      ;; An empty catalog satisfies every per-entry assertion below. Without
      ;; this floor, deleting the catalog would be reported as a clean run.
      (refuse! (str data-path " holds no entries; every per-entry check would "
                    "be vacuously true")))
    [txt data]))

(def date-re #"^\d{4}(-\d{2})?(-\d{2})?$")

;; A wayback capture URL is https://web.archive.org/web/<14-digit stamp>/<original>.
(def archive-re #"^https://web\.archive\.org/web/(\d{14})(?:[a-z_]+)?/(https?://.+)$")

(defn- structural [data]
  (let [ids (map :association-rule/id data)
        dups (->> ids frequencies (keep (fn [[k n]] (when (< 1 n) k))) sort)]
    (concat
     (for [d dups] [:duplicate-id (str d " appears " (count (filter #{d} ids)) " times")])
     (mapcat
      (fn [[i e]]
        (let [at (fn [k] (get e (keyword "association-rule" k)))
              where (str "entry " i " (" (or (at "id") "<no id>") ")")
              f (fn [tag msg] [tag (str where ": " msg)])]
          (concat
           (when-not (string? (at "id")) [(f :missing-key ":id is missing or not a string")])
           (when (and (string? (at "id"))
                      (not (str/starts-with? (at "id") (str ASSOCIATION "."))))
             [(f :id-shape (str ":id must start with \"" ASSOCIATION ".\""))])
           (when-not (and (string? (at "title")) (seq (at "title")))
             [(f :missing-key ":title is missing or empty")])
           (when-not (= ASSOCIATION (at "association"))
             [(f :missing-key (str ":association must be " ASSOCIATION))])
           (when-not (= ISIC (at "isic")) [(f :missing-key (str ":isic must be " ISIC))])
           (when-not (= COUNTRY (at "country")) [(f :missing-key (str ":country must be " COUNTRY))])
           (when-not (keyword? (at "kind")) [(f :missing-key ":kind must be a keyword")])
           (when-not (and (string? (at "url")) (str/starts-with? (at "url") "https://"))
             [(f :url-shape ":url must be an https:// URL")])
           (when-not (keyword? (at "url-provenance"))
             [(f :missing-key ":url-provenance must be a keyword")])
           ;; The URL and the provenance keyword must tell the same story about
           ;; HOW the fact was obtained. Either alone is unfalsifiable.
           (let [u (str (at "url"))
                 p (when (keyword? (at "url-provenance")) (name (at "url-provenance")))
                 claims-archive? (and p (str/ends-with? p "-web-archive"))
                 is-archive? (boolean (re-matches archive-re u))]
             (cond
               (and claims-archive? (not is-archive?))
               [(f :archive-shape
                   (str ":url-provenance says -web-archive but :url is not a "
                        "web.archive.org capture URL: " u))]
               (and is-archive? (not claims-archive?))
               [(f :archive-shape
                   (str ":url is a web.archive.org capture but :url-provenance "
                        "does not end in -web-archive: " p))]
               ;; A capture of some other site would still satisfy both of the
               ;; above while no longer being this association speaking.
               (and is-archive?
                    (not (str/includes? (nth (re-matches archive-re u) 2) ORIGIN-HOST)))
               [(f :archive-shape
                   (str "capture is not of " ORIGIN-HOST ": "
                        (nth (re-matches archive-re u) 2)))]
               ;; A bare origin URL cannot be checked: the host answers 403.
               (and (not is-archive?) (str/includes? u ORIGIN-HOST))
               [(f :archive-shape
                   (str "a bare " ORIGIN-HOST " URL cannot be verified (the host "
                        "answers 403); cite a web.archive.org capture instead"))]
               :else nil))
           (when-not (and (string? (at "source-article")) (seq (at "source-article")))
             [(f :missing-key ":source-article is missing or empty")])
           (when-not (and (string? (at "source-quote")) (seq (at "source-quote")))
             [(f :missing-key (str ":source-quote is missing or empty -- an entry with "
                                   "no quote cannot be checked against its own source"))])
           ;; An article-level rule that does not name its article in the title
           ;; is the drift this catalog is most likely to grow: the quote moves
           ;; to another article and the prose keeps the old number.
           (when (and (string? (at "source-article"))
                      (re-matches #"\d+" (at "source-article"))
                      (string? (at "title"))
                      (not (str/includes? (str/lower (at "title"))
                                          (str "article " (at "source-article")))))
             [(f :article-not-in-title
                 (str "title does not name article " (at "source-article")))])
           (when-not (and (vector? (at "topic")) (seq (at "topic")))
             [(f :missing-key ":topic must be a non-empty vector")])
           (for [d [(at "established-date") (at "last-revised-date") (at "retrieved-at")]
                 :when (and (some? d) (not (re-matches date-re (str d))))]
             (f :date-shape (str "not an ISO date: " d)))
           ;; A date the source does not give may be omitted -- but only out
           ;; loud. An entry that is simply missing both dates and one that
           ;; records why it has none must not read the same.
           (when-not (or (at "established-date") (at "last-revised-date")
                         (at "date-unknown-because"))
             [(f :missing-key (str "needs :established-date or :last-revised-date, "
                                   "or :date-unknown-because naming why the source "
                                   "gives neither"))])
           (when (and (at "date-unknown-because")
                      (not (keyword? (at "date-unknown-because"))))
             [(f :missing-key ":date-unknown-because must be a keyword")])
           (when (and (at "date-unknown-because")
                      (or (at "established-date") (at "last-revised-date")))
             [(f :date-shape (str ":date-unknown-because is set on an entry that "
                                  "does carry a date"))]))))
      (map-indexed vector data)))))

(def ua "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")

(defn- have? [bin]
  (try (cp/execFileSync "sh" #js ["-c" (str "command -v " bin)] #js {:stdio "ignore"}) true
       (catch :default _ false)))

;; Named entities this decoder knows. It is deliberately not the whole HTML5
;; table: what matters here is the Latin-1 letters and the punctuation a source
;; in this family is made of. An incomplete decoder does not weaken this check,
;; it inverts it -- the verbatim span of a page that IS serving the claim comes
;; back as not found, which is exactly how a fabricated citation looks.
(def ^:private named-entities
  {"nbsp" " " "quot" "\"" "apos" "'" "lt" "<" "gt" ">"
   "aacute" "á" "eacute" "é" "iacute" "í" "oacute" "ó" "uacute" "ú"
   "Aacute" "Á" "Eacute" "É" "Iacute" "Í" "Oacute" "Ó" "Uacute" "Ú"
   "agrave" "à" "egrave" "è" "igrave" "ì" "ograve" "ò" "ugrave" "ù"
   "Agrave" "À" "Egrave" "È" "Igrave" "Ì" "Ograve" "Ò" "Ugrave" "Ù"
   "acirc" "â" "ecirc" "ê" "icirc" "î" "ocirc" "ô" "ucirc" "û"
   "Acirc" "Â" "Ecirc" "Ê" "Icirc" "Î" "Ocirc" "Ô" "Ucirc" "Û"
   "auml" "ä" "euml" "ë" "iuml" "ï" "ouml" "ö" "uuml" "ü"
   "Auml" "Ä" "Euml" "Ë" "Iuml" "Ï" "Ouml" "Ö" "Uuml" "Ü"
   "ntilde" "ñ" "Ntilde" "Ñ" "atilde" "ã" "otilde" "õ"
   "Atilde" "Ã" "Otilde" "Õ" "ccedil" "ç" "Ccedil" "Ç"
   "ordf" "ª" "ordm" "º" "deg" "°" "iexcl" "¡" "iquest" "¿"
   "laquo" "«" "raquo" "»" "middot" "·" "shy" "" "sect" "§"
   "ldquo" "\"" "rdquo" "\"" "lsquo" "'" "rsquo" "'"
   "ndash" "–" "mdash" "—" "hellip" "…" "euro" "€" "pound" "£"})

(defn- decode-entities
  "HTML entities -> characters. `&amp;` is decoded LAST, so that a document
   that literally writes `&amp;aacute;` keeps saying `&aacute;` rather than
   silently becoming an accented letter."
  [s]
  (-> s
      (str/replace #"&#(\d+);"
                   (fn [[_ d]] (js/String.fromCodePoint (js/parseInt d 10))))
      (str/replace #"&#[xX]([0-9a-fA-F]+);"
                   (fn [[_ h]] (js/String.fromCodePoint (js/parseInt h 16))))
      (str/replace #"&([a-zA-Z][a-zA-Z0-9]{1,9});"
                   (fn [[whole nm]] (get named-entities nm whole)))
      (str/replace #"&amp;" "&")))

(defn- fetch-text
  "Returns [status text] or [status nil] -- nil text means the body arrived but
   this run could not turn it into text, which is a refusal, not a finding."
  [url]
  (let [tmp (path/join (os/tmpdir) (str "businessnz-src-" (hash url)))
        status (try (str/trim (str (cp/execFileSync
                                    "curl" #js ["-sS" "-L" "--max-time" "120"
                                                "-A" ua "-o" tmp
                                                "-w" "%{http_code}" url]
                                    #js {:encoding "utf8"})))
                    (catch :default e (str "curl-failed: " (.-message e))))
        body (try (fs/readFileSync tmp) (catch :default _ nil))
        pdf? (and body (str/starts-with? (.toString (.slice body 0 5) "utf8") "%PDF-"))
        text (cond
               (nil? body) nil
               pdf? (when (have? "pdftotext")
                      (try (str (cp/execFileSync "pdftotext" #js [tmp "-"]
                                                 #js {:encoding "utf8"
                                                      :maxBuffer 33554432}))
                           (catch :default _ nil)))
               :else (-> (.toString body "utf8")
                         (str/replace #"(?is)<(script|style|noscript)[^>]*>.*?</\1>" " ")
                         (str/replace #"(?s)<[^>]+>" " ")
                         decode-entities))]
    (try (fs/unlinkSync tmp) (catch :default _ nil))
    [status (when text (str/replace text #"\s+" " "))]))

(defn- run-live [data]
  (when-not (have? "curl") (refuse! "curl is not on PATH"))
  (let [urls (vec (distinct (map :association-rule/url data)))
        fetched (reduce (fn [m u] (assoc m u (fetch-text u))) {} urls)
        unreadable (for [[u [status text]] fetched
                         :when (or (not (re-matches #"2\d\d" status)) (nil? text))]
                     (str u " -> status=" status
                          (when (nil? text) (str " (body could not be turned into text"
                                                 (when-not (have? "pdftotext")
                                                   "; pdftotext is not on PATH")
                                                 ")"))))]
    (println (str "FETCHED\t" (- (count urls) (count unreadable)) "/" (count urls)))
    (when (seq unreadable)
      ;; Every quote check below would be "not found", which reads exactly like
      ;; a fabricated citation. Refuse instead of accusing the catalog.
      (refuse! (str "could not read " (count unreadable) " of " (count urls)
                    " sources:\n  " (str/join "\n  " unreadable))))
    (keep (fn [e]
            (let [u (:association-rule/url e)
                  q (str/replace (str (:association-rule/source-quote e)) #"\s+" " ")
                  [_ text] (get fetched u)]
              (when-not (str/includes? text q)
                [:quote-not-in-source
                 (str (:association-rule/id e) ": :source-quote is not in " u
                      "\n      quote: " q)])))
          data)))

(let [[txt data] (read-catalog)
      findings (concat (structural data) (when live? (run-live data)))]
  (println (str "SCANNED\t" (count data) " entries, "
                (count (re-seq #"https?://" txt)) " citations, "
                (count (distinct (map :association-rule/url data))) " distinct sources"
                (if live? ", live" ", structural only")))
  (doseq [[tag msg] findings] (println (str "  [" (name tag) "] " msg)))
  (if (seq findings)
    (do (println (str (count findings) " finding(s)")) (.exit js/process 1))
    (do (println "ok") (.exit js/process 0))))
