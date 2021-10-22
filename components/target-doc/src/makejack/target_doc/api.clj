(ns makejack.target-doc.api
  "Provide documentation on namespace public functions"
  (:require
   [clojure.pprint :as pprint]
   [clojure.string :as str]))


(defn- meta-less
  "Order alphabetically with values with the same :doc-order."
  [a b]
  (let [a-order (:doc-order a)
        b-order (:doc-order b)]
    (or (and a-order (nil? b-order))
        (and b-order a-order (< a-order b-order))
        (and (= a-order b-order)
             (neg? (compare (:name a) (:name b)))))))

(def ^:private meta-comparator
  (comparator meta-less))

(defn- doc-map [fn-meta]
  (-> fn-meta
      (select-keys [:arglists :params :doc :doc-order :name])
      (update :name name)
      (update :doc #(some->>
                     %
                     str/split-lines
                     (map str/trim)
                     (str/join "\n")))))

(defn ns-doc
  "Return a sequence of doc maps for the given namespace.

  Return an entry for each public function, that doesn't have :no-doc
  meta.  Functions will be sorted alphabetically with values with the
  same :doc-order meta value.  Any functions with :doc-order meta appear
  before entries without :doc-order meta."
  [ns-sym]
  (->> (ns-publics ns-sym)
       vals
       (map meta)
       (remove :no-doc)
       (filter :arglists)
       (sort meta-comparator)
       (map doc-map)))

(defn first-doc-line [f-doc]
  (some-> f-doc :doc str/split-lines first))

(defn fn-summary
  [name-length ns-doc]
  (format
   (str "%" name-length "s  %s")
   (:name ns-doc)
   (or (first-doc-line ns-doc) "")))

(defn help-summary [ns-doc]
  (let [names (map :name ns-doc)
        l     (reduce max (map count names))]
    (->> ns-doc
         (map (partial fn-summary l))
         (str/join "\n"))))

(defn params-str [{:keys [arglists params]}]
  (or
   params
   (let [arglist (first arglists)]
     (when-let [arg (first arglist)]
       (prn :arg arg)
       (if-let [kws (and (map? arg) (:keys arg))]
         ;; destructured keys
         (str/join (map #(format "[:%s arg]" %) kws))
         (when (= '_  arg)
           ""))))
   ;; provide a default
   "[kwargs ...]"))

(defn fn-help [fn-doc]
  (format
   "%s %s\n\n%s\n"
   (:name fn-doc)
   (params-str fn-doc)
   (:doc fn-doc)))

(defn fn-doc [ns-doc fn-name]
  (->> ns-doc
       (filter #(= (str fn-name) (:name %)))
       first))


(defn help-task
  "Provide a help task that summarises the namespace public functions.

  Return a string with the summary.

  Use ^:no-doc meta to suppress the output of a function.

  Use ^:params to provide a one line description of the parameters.
  supported."
  [{:keys [target]} ns-ref]
  ;; TODO: make this a function in target-doc
  (let [ns-doc (ns-doc ns-ref)]
    (if target
      (if-let [fn-doc (fn-doc ns-doc target)]
        (fn-help fn-doc)
        (str "Unknown target:" target))
      (str
       (help-summary ns-doc)
       "\n\nUse `help :target <target-name>` for detailed help on a target."))))
