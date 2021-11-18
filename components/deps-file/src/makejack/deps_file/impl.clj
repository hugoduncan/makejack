(ns makejack.deps-file.impl
  (:require
   [babashka.fs :as fs]
   [rewrite-clj.node :as n]
   [rewrite-clj.zip :as z]))



;; (defn update-dep-value
;;   [artifact-name update-vals src]
;;   (-> (z/of-string src)
;;       spy
;;       (z/find-depth-first
;;        #(do (prn :a (str artifact-name) (n/string (z/node %)))
;;             (spy %)(= (str artifact-name) (n/string (z/node %)))))
;;       spy
;;       z/right
;;       z/down
;;       spy
;;       (z/find z/right
;;               #(do
;;                  (prn :x)(spy %)
;;                  (= (str ":git/sha") (n/string (z/node %)))))
;;       z/right
;;       (z/replace (n/token-node (:git/sha update-vals)))
;;       z/root-string))


;; (defn update-dep-tags
;;   [z update-vals]
;;   (-> (z/of-string src)
;;       spy
;;       (z/find-depth-first
;;        #(do (prn :a (str artifact-name) (n/string (z/node %)))
;;             (spy %)(= (str artifact-name) (n/string (z/node %)))))
;;       spy
;;       z/right
;;       z/down
;;       spy
;;       (z/find z/right
;;               #(do
;;                  (prn :x)(spy %)
;;                  (= (str ":git/sha") (n/string (z/node %)))))
;;       z/right
;;       (z/replace (n/token-node (:git/sha update-vals)))
;;       z/root-string))


(defn spy
  ([x] (spy x :spy))
  ([x label]
   (if x
     (when-let [node (z/node x)]
       (prn label :node (n/tag node) (n/string node)))
     (prn label :no-location))
   x))

(defn kw=?
  [kw]
  (fn [z]
    (when-let [node (z/node z)]
      (and (= :token (n/tag node))
           (= (str kw) (n/string node))))))

(defn string=?
  [s]
  (fn [z]
    (when-let [node (z/node z)]
      (and (= :token (n/tag node))
           (= s (n/string node))))))

(defn- update-kv
  [k v z]
  (prn :update-kv (n/sexpr z) k v)
  (if-let [zz (z/get z k)]
    (z/replace zz (n/token-node v))
    z))

(defn- update-in-artifact
  [z update-vals]
  (prn :update-in-artifact (n/sexpr z))
  (loop [z           z
         update-vals update-vals]
    (let [[[k v] & more] update-vals]
      (if k
        (recur (z/subedit-node z (partial update-kv k v)) more)
        z))))

(defn- update-in-deps-val
  [z artifact-name update-vals]
  (prn :update-in-deps-val (n/sexpr z))
  (if-let [zz (z/get z artifact-name;; #((string=? artifact-name)
                     ;;   (spy % :look-for-artifact))
                     )]
    (z/subedit-node zz #(update-in-artifact % update-vals))
    z))

(defn update-in-deps-keys
  [src artifact-name update-vals]
  (loop [z (z/of-string src)]
    (if-let [zz (z/find-depth-first
                 z
                 #((kw=? :deps) (spy % :look-for-dep-key)))]
      (recur
       (->
        (z/right zz)
        (z/subedit-node
         #(update-in-deps-val % artifact-name update-vals))
        ))
      (z/root-string z))))

(defn project-edn-path
  ^java.nio.file.Path [{:keys [dir]}]
  (fs/path (or dir ".") "project.edn"))

(defn deps-edn-path
  ^java.nio.file.Path [{:keys [dir]}]
  (fs/path (or dir ".") "deps.edn"))

(defn update-dep
  [{:keys [artifact-name] :as params}]
  (assert artifact-name "must pass :artifact-name")
  (assert (symbol? artifact-name) ":artifact-name must have a symbol as value")
  (let [f           (-> params deps-edn-path fs/file)
        src         (slurp f)
        _           (prn :xx f)
        update-vals (select-keys params [:git/sha :git/tag :mvn/version])
        new-s       (update-in-deps-keys src artifact-name update-vals)]
    (if new-s
      (spit f new-s)
      (throw (ex-info
              "Failed to write deps.edn"
              {:path (str f)})))))
