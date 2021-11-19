(ns makejack.deps-file.impl
  (:require
   [babashka.fs :as fs]
   [rewrite-clj.node :as n]
   [rewrite-clj.zip :as z]))

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
  (if-let [zz (z/get z k)]
    (z/replace zz (n/token-node v))
    z))

(defn- update-in-artifact
  [z update-vals]
  (loop [z           z
         update-vals update-vals]
    (let [[[k v] & more] update-vals]
      (if k
        (recur (z/subedit-node z (partial update-kv k v)) more)
        z))))

(defn- update-in-deps-val
  [z artifact-name update-vals]
  (if-let [zz (z/get z artifact-name)]
    (z/subedit-node zz #(update-in-artifact % update-vals))
    z))

(defn update-in-deps-keys
  [src artifact-name update-vals]
  (loop [z (z/of-string src)]
    (if-let [zz (z/find-depth-first z (kw=? :deps))]
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
        update-vals (select-keys params [:git/sha :git/tag :mvn/version])
        new-s       (update-in-deps-keys src artifact-name update-vals)]
    (if new-s
      (spit f new-s)
      (throw (ex-info
              "Failed to write deps.edn"
              {:path (str f)})))))
