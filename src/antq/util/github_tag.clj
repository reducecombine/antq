(ns antq.util.github-tag
  (:require
   [clojure.string :as str]))

(defn exact-or-included
  [coll ^String target]
  (or (some #(and (= target %) %) coll)
      (first (filter #(str/includes? % target) coll))))
