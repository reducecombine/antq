(ns antq.changelog)

(defmulti get-changelog-url
  (fn [version-checked-dep]
    (:type version-checked-dep)))

(defmethod get-changelog-url :default
  [_dep]
  nil)
