(ns makejack.util.file-changed.impl
  (:require
   [clojure.java.io :as io]
   [makejack.util.filesystem :as fs]
   [makejack.util.path :as path])
  (:import
   [java.nio.charset
    StandardCharsets]
   [java.security
    DigestInputStream
    MessageDigest]))

(def HEX-CHARS
  (.getBytes "0123456789ABCDEF" (StandardCharsets/US_ASCII)))

(defn hex-string [^bytes bytes]
  (let [l         (alength bytes)
        hex-chars (byte-array (* 2 l))]
    (loop [j 0
           k 0]
      (let [v  (bit-and (aget bytes j) 0xff)
            j1 (unchecked-inc j)
            k1 (unchecked-inc k)]
        (aset hex-chars k (aget HEX-CHARS (bit-shift-right v 4)))
        (aset hex-chars k1 (aget HEX-CHARS (bit-and v 0x0f)))
        (when (< j1 l)
          (recur j1 (unchecked-inc k1) ))))
    (String. hex-chars StandardCharsets/UTF_8)))

(defn hash [path-like]
  (let [md    (MessageDigest/getInstance "MD5")
        n     (* 128 1024)
        bytes (byte-array n)]
    (with-open [dis (DigestInputStream.
                     (io/input-stream
                      (path/as-file path-like))
                     md)]
      (loop []
        (when (pos? (.read dis bytes 0 n))
          (recur)))
      (-> (.digest md)
          (hex-string)))))

(defn changed-file-hash
  "Return the changed file info if it has been modified."
  [file-info path-like]
  (let [k                       (str (path/as-path path-like))
        {:keys [hash modified]} (get file-info k)
        now-modified            (fs/last-modified path-like)]
    (when-not (= modified now-modified)
      (let [new-hash (hash path-like)]
        (when-not (= hash new-hash)
          [k {:hash     new-hash
              :modified now-modified}])))))
