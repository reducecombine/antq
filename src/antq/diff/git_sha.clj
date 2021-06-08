(ns antq.diff.git-sha
  (:require
   [antq.diff :as diff]
   [antq.log :as log]
   [antq.util.url :as u.url]))

(defmethod diff/get-diff-url :git-sha
  [dep]
  (when-let [url (get-in dep [:extra :url])]
    (cond
      (re-find #"https?://github.com/" url)
      (format "%scompare/%s...%s"
              (-> url
                  (u.url/ensure-git-https-url)
                  (u.url/ensure-https)
                  (u.url/ensure-tail-slash))
              (:version dep)
              (:latest-version dep))

      :else
      (log/error (str "Diff is not supported for " url)))))
