(ns makejack.verbose.impl)

(defn println-when-verbose
  [{:keys [verbose]} args]
  (when verbose
    (apply println args)))
