(ns makejack.api.babashka
  (:require [clojure.string :as str]
            [makejack.api.clojure-cli :as clojure-cli]
            [makejack.api.core :as makejack]))

(defn process
  "Execute babashka process.

  args is a vector of arguments to pass.

  options is a map of options, as specifed in babashka.process/process.
  Defaults to {:err :inherit}."
  [aliases deps args options]
  (let [cp   (cond-> ""
               (or (:with-project-deps? options)
                   deps
                   aliases)
               (str ":" (clojure-cli/classpath aliases deps options)))
        args (cond-> ["bb"]
               (not (str/blank? cp)) (into ["-cp" cp])
               args                  (into args))]
    (makejack/process args options)))
