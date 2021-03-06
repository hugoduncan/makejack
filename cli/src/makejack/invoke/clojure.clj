(ns makejack.invoke.clojure
  "Makejack tool to invoke clojure"
  (:require [makejack.api.clojure-cli :as clojure-cli]
            [makejack.impl.util :as util]))

(def ^:private option-str->keyword
  {"-Sdeps" :sdeps
   "-A"     :aliases})

(defn- parse-args [args]
  (loop [args args
         res  {}]
    (let [arg (first args)]
      (if-let [kw (option-str->keyword arg)]
        (recur
         (fnext args)
         (assoc-in res [:tool-options kw] (ffirst args)))
        (assoc res :tool-args args)))))

(defn clojure
  "Execute clojure"
  [args target-kw {:keys [mj project] :as _config} options]
  (let [tool-options     (:tool-options (parse-args args))
        target-config    (some-> mj :targets target-kw)
        aliases          (-> []
                             (into (:aliases target-config))
                             (into (:aliases project))
                             (into (:aliases options))
                             (into (:aliases tool-options)))
        deps-edn         (select-keys target-config [:deps])
        options          (merge options (:options target-config))
        forward-options? (:forward-options options true)
        repro?           (:repro options true)
        report           (:report options "stderr")
        args             (concat
                          (clojure-cli/args
                           {:deps  (merge deps-edn
                                          (:Sdeps tool-options))
                            :repro repro?})
                          (clojure-cli/main-args
                           {:report    report
                            :aliases   aliases
                            :expr      (:expr target-config)
                            :main      (:main target-config)
                            :main-args (cond-> []
                                         forward-options?
                                         (into ["-o" (dissoc options :dir)])
                                         true (into
                                               (:main-args target-config)))})
                          args)]
    (try
      (clojure-cli/process args options)
      (catch clojure.lang.ExceptionInfo e
        (util/handle-invoker-exception e)))))
