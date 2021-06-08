(ns antq.diff.java
  (:require
   [antq.diff :as diff]
   [antq.log :as log]
   [antq.util.git :as u.git]
   [antq.util.java :as u.java]
   [antq.util.url :as u.url]
   [clojure.string :as str]))

(defmethod diff/get-diff-url :java
  [dep]
  (when-let [url (u.java/get-scm-url dep)]
    (cond
      (re-find #"https?://github.com/" url)
      (let [tags (u.git/tags-by-ls-remote url)
            current (first (filter #(str/includes? % (:version dep)) tags))
            latest (or (first (filter #(str/includes? % (:latest-version dep)) tags))
                       ;; If there isn't a tag for latest version
                       "head")]
        (if current
          (format "%scompare/%s...%s"
                  (-> url u.url/ensure-https u.url/ensure-tail-slash)
                  current
                  latest)
          (do (log/error (str "The tag for current version is not found: " url))
              ;; not diff, but URL is useful for finding the differences.
              nil)))

      :else
      (do (log/error (str "Diff is not supported for " url))
          ;; not diff, but URL is useful for finding the differences.
          nil))))
