(ns antq.changelog.java
  (:require
   [antq.changelog :as changelog]
   [antq.log :as log]
   [antq.util.git :as u.git]
   [antq.util.java :as u.java]
   [antq.util.url :as u.url]
   [clojure.string :as str]))

(defmethod changelog/get-changelog-url :java
  [dep]
  (when-let [url (u.java/get-scm-url dep)]
    (cond
      (re-find #"https?://github.com/" url)
      (let [repo-path (str/replace url #"https?://github.com/" "")
            tag (->> url
                     (u.git/tags-by-ls-remote)
                     (filter #(str/includes? % (:latest-version dep)))
                     first)
            clean-url (-> url u.url/ensure-https u.url/ensure-tail-slash)
            contents (if tag
                       ;; XXX memo
                       (slurp (str clean-url "/tree/" tag))
                       (slurp (str clean-url)))
            found? (and contents
                        (not (str/includes? contents "This is not the web page you are looking for")))
            tagged-file-ref-re (when found?
                                 (re-pattern (str "(?<=href=\"/)"
                                                  repo-path
                                                  "blob/("
                                                  (or tag
                                                      ;; an arbitrary word that will mever match anything:
                                                      "xxxxxxxx")
                                                  "|master)/(changes|changelog|CHANGES|CHANGELOG)\\.(md|adoc|txt])")))
            [filename] (when found?
                         (or (some-> tagged-file-ref-re
                                     (re-find contents))
                             (when-let [[v] (re-find #"(?<=href=\")#(change-?log|changes)" contents)]
                               (if tag
                                 [(str "tree/" tag v)]
                                 v))))]
        (if filename
          (if-not tag
            (str "https://github.com/" filename)
            (str clean-url filename))
          (do
            (log/error (str "No changelog could be found for: " url))
            nil)))

      :else
      (do (log/error (str "Changelog is not supported for " url))
          nil))))
