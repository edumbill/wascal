(ns wascal
  (:require [clojure.browser.repl :as repl]
            [clojure.string :as string]
            [goog.dom :as dom]
))

;; with thanks to https://github.com/bnbeckwith/writegood-mode

(defn ^:export connectrepl []
  (repl/connect "http://localhost:9000/repl"))

(def weasel-words
  ["many" "various" "very" "fairly" "several" "extremely" 
   "exceedingly" "quite" "remarkably" "few" "surprisingly" 
   "mostly" "largely" "huge" "tiny" "are a number" "is a number" 
   "excellent" "interestingly" "significantly" "substantially" 
   "clearly" "vast" "relatively" "completely"])

(def passive-voice-verbs
  ["am" "are" "were" "being" "is" "been" "was" "be"])

(def passive-voice-irregulars
  ["awoken" "been" "born" "beat" "become" "begun" "bent" "beset" 
   "bet" "bid" "bidden" "bound" "bitten" "bled" "blown" "broken" 
   "bred" "brought" "broadcast" "built" "burnt" "burst" "bought" 
   "cast" "caught" "chosen" "clung" "come" "cost" "crept" "cut" 
   "dealt" "dug" "dived" "done" "drawn" "dreamt" "driven" "drunk" 
   "eaten" "fallen" "fed" "felt" "fought" "found" "fit" "fled" 
   "flung" "flown" "forbidden" "forgotten" "foregone" "forgiven" 
   "forsaken" "frozen" "gotten" "given" "gone" "ground" "grown" 
   "hung" "heard" "hidden" "hit" "held" "hurt" "kept" "knelt" "knit" 
   "known" "laid" "led" "leapt" "learnt" "left" "lent" "let" "lain" 
   "lighted" "lost" "made" "meant" "met" "misspelt" "mistaken" "mown" 
   "overcome" "overdone" "overtaken" "overthrown" "paid" "pled" "proven" 
   "put" "quit" "read" "rid" "ridden" "rung" "risen" "run" "sawn" 
   "said" "seen" "sought" "sold" "sent" "set" "sewn" "shaken" "shaven" 
   "shorn" "shed" "shone" "shod" "shot" "shown" "shrunk" "shut" 
   "sung" "sunk" "sat" "slept" "slain" "slid" "slung" "slit" 
   "smitten" "sown" "spoken" "sped" "spent" "spilt" "spun" "spit" 
   "split" "spread" "sprung" "stood" "stolen" "stuck" "stung" 
   "stunk" "stridden" "struck" "strung" "striven" "sworn" "swept" 
   "swollen" "swum" "swung" "taken" "taught" "torn" "told" "thought" 
   "thrived" "thrown" "thrust" "trodden" "understood" "upheld" "upset" 
   "woken" "worn" "woven" "wed" "wept" "wound" "won" "withheld" 
   "withstood" "wrung" "written"])


(comment
  ;; when the repl's connected, use this in the clojure repl
  ;; to kick off the clojurescript repl
  (require '[cljs.repl :as repl])
  (require '[cljs.repl.browser :as browser])
  (def env (browser/repl-env))
  (repl/repl env)
  ;; then
  (ns wascal
    (:require [clojure.browser.repl :as repl]
              [clojure.string :as string]
              [goog.dom :as dom]
              ))
  ;;
  )

(def weasel-words-regex
  (js/RegExp. (str "(" (string/join "|" weasel-words) ")") "igm"))

(def passive-voice-regex
  (js/RegExp. (str "\\b(" (string/join "|" passive-voice-verbs) ")\\b\\s*"
                   "(\\w+ed|" (string/join "|" passive-voice-irregulars)
                   ")\\b") "igm"))

(def duplicate-word-regex
  (js/RegExp. "\\b(\\w+)\\s*\\b\\1\\b" "igm"))

(defn all-first-matches [regex s]
  (map first (re-seq regex s)))

(defn ^:export find-problems [s]
  (flatten (map #(all-first-matches %1 s)
        [passive-voice-regex
         weasel-words-regex
         duplicate-word-regex])))

;; UI stuff

(defn make-error-list [probs]
  (let [ul (dom/createDom "ul")
        items (map #(dom/createDom "li" nil (dom/createTextNode %1))
                   probs)]
    (doseq [item items]
      (dom/appendChild ul item))
    ul))

(defn ^:export demo-find-problems []
  (let [probs (find-problems
               (.value (dom/getElement "source")))
        results (dom/getElement "results")]
    (if (empty? probs)
      (dom/setTextContent results "No errors found.")
      (do (dom/removeChildren results)
          (dom/appendChild results (make-error-list probs)))
      )
    ))

(defn ^:export setup-demo []
  (goog.events/listen (dom/getElement "theform")
                      goog.events.EventType/SUBMIT
                      (fn [e]
                        (. e (preventDefault))
                        (demo-find-problems))))
