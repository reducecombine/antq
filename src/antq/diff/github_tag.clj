(ns antq.diff.github-tag
  (:require
   [antq.diff :as diff]
   [antq.util.git :as u.git]
   [antq.util.github-tag :as u.github-tag]
   [clojure.string :as str]))

(defmethod diff/get-diff-url :github-tag
  [dep]
  (let [url (format "https://github.com/%s"
                    (str/join "/" (take 2 (str/split (:name dep) #"/"))))
        tags (u.git/tags-by-ls-remote url)
        current (or (u.github-tag/exact-or-included tags (:version dep))
                    (:version dep))
        latest (or (u.github-tag/exact-or-included tags (:latest-version dep))
                   (:latest-version dep))]
    (format "%s/compare/%s...%s" url current latest)))
