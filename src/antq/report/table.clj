(ns antq.report.table
  (:require
   [antq.report :as report]
   [antq.util.dep :as u.dep]
   [antq.util.ver :as u.ver]
   [clojure.pprint :as pprint]
   [clojure.set :as set]))

(defn skip-duplicated-file-name
  [sorted-deps]
  (loop [[dep & rest-deps] sorted-deps
         last-file nil
         result []]
    (if-not dep
      result
      (if (= last-file (:file dep))
        (recur rest-deps last-file (conj result (assoc dep :file "")))
        (recur rest-deps (:file dep) (conj result dep))))))

(defmethod report/reporter "table"
  [deps _options]
  ;; Show table
  (if (seq deps)
    (->> deps
         (sort u.dep/compare-deps)
         skip-duplicated-file-name
         (map #(assoc % :latest-version (u.ver/normalize-latest-version %)))
         (map #(let [latest-key (if (seq (:latest-name %))
                                  :latest-name
                                  :latest-version)]
                 (set/rename-keys % {:version :current
                                     latest-key :latest})))
         (pprint/print-table [:file :name :current :latest]))
    (println "All dependencies are up-to-date."))

  ;; Show diff URLs, changelogs, in a grouped-by-dep fashion
  (let [corpus (->> deps
                    (filter :latest-version)
                    (sort u.dep/compare-deps)
                    (filter (some-fn :diff-url :changelog-url))
                    (group-by (juxt :type :name :version))
                    (vals))]
    (when (seq corpus)
      (println "\nAvailable update information:")
      (doseq [group corpus
              :let [urls (->> group (keep :diff-url) (distinct))
                    changelogs (->> group (keep :changelog-url) (distinct))]]
        (doseq [c changelogs]
          (println "-" c))
        (doseq [u urls]
          (println "-" u))))))
