(ns makejack.path.api
  "Path manipulation functions"
  (:import
   [java.io File]
   [java.nio.file
    Path
    Paths]))

(def ^:private ^"[Ljava.lang.String;" empty-strings (make-array String 0))

;; "Coerce between various 'resource-namish' things."
(defprotocol Coercions
  (as-path ^{:tag java.nio.file.Path} [x]
    "Coerce argument to a Path."))

(extend-protocol Coercions
  nil
  (as-path [_] nil)

  Path
  (as-path [p] p)

  String
  (as-path [s] (Paths/get s empty-strings))

  File
  (as-path [f] (.toPath f))

  java.net.URL
  (as-path [f] (.getPath f)))

(defn as-file ^File [^Path path-like]
  (.toFile (as-path path-like)))

(defn path
  "Return a java.nio.file.Path, passing each argument to as-path.

  Multiple-arg versions treat the first argument as parent and
  subsequent args as children relative to the parent."
  (^Path [path-like]
   (as-path path-like))
  (^Path [parent child]
   (.resolve (as-path parent) (str child)))
  (^Path [parent child & more]
   (reduce path (path parent child) more)))

(defn path-for [& path-components]
  (apply path (filter some? path-components)))

(defn path?
  "Predicate for x being a Path object."
  [x]
  (instance? Path x))

(defn filename
  "Return the filename segment of the given path as a Path."
  ^Path [path-like]
  (.getFileName (path path-like)))

(defn parent
  "Return the parent directory of a the given path as a Path"
  ^Path [path-like]
  (.getParent (path path-like)))

(defn absolute?
  "Predicate for an absolute path."
  [path-like]
  (.isAbsolute (path path-like)))

(defn normalize
  "Remove redundant path elements."
  [path-like]
  (.normalize (path path-like)))

(defn path-with-extension
  "Return the path with extension added to it.
  The extension is a string, including any required dot."
  ^Path [path-like extension]
  (let [^Path base-path (path path-like)
        parent-path     (.getParent base-path)
        filename        (str (.getFileName base-path) extension)]
    (if parent-path
      (path parent-path filename)
      (path filename))))

(defn relative-to
  "Return a function that returns its argument path relative to the given root."
  [root]
  (let [root (path root)]
    (fn ^Path [p] (.relativize ^Path root (path p)))))
