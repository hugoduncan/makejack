(ns makejack.filesystem.api
  "File system manipulation functions."
  (:require
   [clojure.java.io :as io]
   [makejack.filesystem.impl :as impl]
   [makejack.path.api :as path])
  (:import
   [java.io File]
   [java.nio.file
    CopyOption
    FileVisitOption
    Files
    Path
    StandardCopyOption]
   [java.nio.file.attribute
    FileAttribute]))

(defn real-path
  "Resolve the path to a path that is represented on the filesystem.
  Return a path."
  ^Path [path-like]
  (.toRealPath (path/path path-like) impl/dont-follow-links))

(defn chmod
  "Change file mode, given octal mode specification as string."
  [path-like mode]
  (let [specs (map impl/char-to-int mode)
        perms (reduce
               (fn [perms [who spec]]
                 (cond-> perms
                   (pos? (bit-and spec 1))
                   (conj (first (impl/POSIX-PERMS who)))
                   (pos? (bit-and spec 2))
                   (conj (second (impl/POSIX-PERMS who)))
                   (pos? (bit-and spec 4))
                   (conj (last (impl/POSIX-PERMS who)))))
               #{}
               (map vector [:owner :group :others] specs))]
    (Files/setPosixFilePermissions
     (path/path path-like)
     perms)))

(defn mkdirs
  "Ensure the given path exists."
  [path-like]
  (Files/createDirectories (path/path path-like) (make-array FileAttribute 0)))

(defn cwd
  "Rturn the current working directory as a Path."
  ^Path []
  (.toAbsolutePath (path/path ".")))

(defn file-exists?
  "Predicate for the given path existing."
  [path-like]
  (Files/exists (path/path path-like) impl/link-options))

(defn file?
  "Predicate for path referring to a file."
  [path-like]
  (.isFile (.toFile (path/path path-like))))

(defn directory?
  "Predicate for path referring to a directory."
  [path-like]
  (.isDirectory (.toFile (path/path path-like))))

(defn last-modified
  "Return last modified time of the given path"
  [path-like]
  (.lastModified (path/as-file path-like)))

(def copy-option-values
  {:copy-attributes  StandardCopyOption/COPY_ATTRIBUTES
   :replace-existing StandardCopyOption/REPLACE_EXISTING})

(defn copy-options
  ^"[Ljava.nio.file.CopyOption;" [{:keys [copy-attributes replace-existing]
                                   :as   options}]
  (into-array
   CopyOption
   (reduce
    (fn [opts [kw option-value]]
      (if (kw options)
        (conj opts option-value)
        opts))
    []
    copy-option-values)))

(def visit-option-values
  {:follow-links FileVisitOption/FOLLOW_LINKS})

(defn visit-options
  ^"[Ljava.nio.file.FileVisitOption;" [{:keys [follow-links] :as options}]
  (into-array
   FileVisitOption
   (reduce
    (fn [opts [kw option-value]]
      (if (kw options)
        (conj opts option-value)
        opts))
    []
    visit-option-values)))

;; (defn visit-option-set
;;   ^java.util.Set [{:keys [follow-links] :as options}]
;;   (reduce
;;    (fn [^java.util.Set opts [kw option-value]]
;;      (if (kw options)
;;        (.add opts option-value))
;;      opts)
;;    (java.util.HashSet.)
;;    visit-option-values))

(defn list-paths
  "Return a lazy sequence of paths under path in depth first order."
  ([path-like]
   (list-paths path-like {}))
  ([path-like {:keys [follow-links] :as options}]
   (->> (Files/walk (path/path path-like) (visit-options options))
        (.iterator)
        iterator-seq))
  ;; [path-like]
  ;; (->> (file-seq (.toFile (path/path path-like)))
  ;;      (map path/path))
  )

(defn copy-file!
  ([source-path target-path]
   (copy-file! source-path target-path {:copy-attributes true}))
  ([source-path target-path {:keys [copy-attributes replace-existing] :as options}]
   (Files/copy
    (path/path source-path)
    (path/path target-path)
    (copy-options options))))

(defn copy-files!
  ([source-path target-path]
   (copy-files! source-path target-path {:copy-attributes true}))
  ([source-path
    target-path
    {:keys [copy-attributes replace-existing follow-links] :as options}]
   (let [source-path   (path/path source-path)
         relative-path (path/relative-to source-path)
         copy-options  (copy-options options)]
     (doseq [p (list-paths source-path)]
       (if (directory? p)
         (mkdirs (path/path target-path (relative-path p)))
         (copy-file!
          p
          (path/path target-path (relative-path p))
          copy-options))))))

(defn delete-file!
  "Delete the file at the specified path-like.
  Semantics as for java.nio.file.Files/delete."
  [path-like]
  (Files/delete (path/path path-like)))

(defn delete-recursively!
  [path-like]
  (let [paths (->> (list-paths path-like)
                   (sort-by identity (comp - compare))
                   vec)]
    (doseq [^Path path paths]
      (.delete (.toFile path)))))

(defn delete-on-exit-if-exists! [path-like]
  (let [path (path/path path-like)]
    (-> (java.lang.Runtime/getRuntime)
        (.addShutdownHook
         (Thread.
          (fn []
            (when (file-exists? path)
              (delete-recursively! path)
              (delete-file! path))))))))


(defn ^:no-doc with-bindings-macro
  [bindings body macro-sym macro-fn]
  {:pre [(vector? bindings) (even? (count bindings))]}
  (cond
    (not (seq bindings))   `(do ~@body)
    (symbol? (bindings 0)) (macro-fn
                            (subvec bindings 0 2)
                            [`(~macro-sym
                               ~(subvec bindings 2)
                               ~@body)])
    :else                  (throw
                            (IllegalArgumentException.
                             (str (name macro-sym)
                                  " only allows [symbol value] pairs in bindings")))))

(defn make-temp-path
  "Return a temporary file path.

  The options map can pass the keys:

  :delete-on-exit - delete the file on JVM exit (default false)
  :dir - the directory in which to create the file (defaults to the system temp dir).
         Must be a path-like.
  :prefix - prefix for the file name (default \"tmp\").
            Must be at elast three characters long.
  :suffix - suffix for the file name (default \".tmp\")

  As a shortcut, a prefix string can be passed instead of the options mao."
  ^Path [options-or-prefix]
  (let [{:keys [delete-on-exit dir prefix suffix] :or {suffix ".tmp"}}
        (if (map? options-or-prefix)
          options-or-prefix)
        prefix (or prefix
                   (and (string? options-or-prefix) options-or-prefix)
                   "tmp")
        path   (if dir
                 (Files/createTempFile
                  (path/path dir)
                  prefix
                  suffix
                  impl/empty-file-attributes)
                 (Files/createTempFile
                  prefix
                  suffix
                  impl/empty-file-attributes))]
    (when delete-on-exit
      (delete-on-exit-if-exists! path))
    path))

(defn ^:no-doc with-temp-path-fn
  [[sym prefix-or-options] body]
  `(let [~sym    (make-temp-path ~prefix-or-options)
         delete# (if (map? ~prefix-or-options)
                   (:delete ~prefix-or-options true)
                   true)]
     (try
       ~@body
       (finally
         (if delete#
           (delete-file! ~sym))))))

(defmacro with-temp-path
  "A scope with sym bound to a java.io.File object for a temporary
  file in the system's temporary directory.

  Options is a map with the keys:

  :delete - delete file when leaving scope (default true)
  :delete-on-exit - delete the file on JVM exit (default false)
  :dir - directory to create the file in (default is the system temp dir).
         Must be of type that can be passed to clojure.java.io/file.
  :prefix - prefix for the file name (default \"tmp\")
            Must be at elast three characters long.
  :suffix - suffix for the file name (default \".tmp\")"
  [bindings & body]
  (with-bindings-macro bindings body `with-temp-path with-temp-path-fn))

(defn ^File make-temp-dir
  "Return a newly created temporary directory.
  Prefix is an arbitary string that is used to name the directory.
  Options is a map with the keys:
  :delete-on-exit - delete the dir on JVM exit (default true).
  :dir - directory to create the dir in (default is the system temp dir).
         Must be of type that can be passed to clojure.java.io/dir.
  :prefix - a string that is used to name the directory."
  [prefix-or-options]
  ^Path {:pre [(or (string? prefix-or-options) (map? prefix-or-options))]}
  (let [prefix          (if (string? prefix-or-options)
                          prefix-or-options
                          (:prefix prefix-or-options))
        {:keys [delete-on-exit dir]
         :or   {delete-on-exit true}
         :as   options} (if (map? prefix-or-options) prefix-or-options {})
        _               (assert (string? prefix))
        _               (assert (map? options))
        dir             (if dir (.toPath (io/file dir)))
        file-attributes (into-array FileAttribute [])
        file            (..
                         (if dir
                           (Files/createTempDirectory dir prefix file-attributes)
                           (Files/createTempDirectory prefix file-attributes))
                         (toFile))]
    (when delete-on-exit
      (-> (java.lang.Runtime/getRuntime)
          (.addShutdownHook
           (Thread.
            (fn []
              (when (file-exists? file)
                (delete-recursively! file)
                (.delete file)))))))
    file))

(defn ^:no-doc with-temp-dir-fn
  [[sym prefix-or-options] body]
  `(let [~sym (make-temp-dir ~prefix-or-options)]
     (try
       ~@body
       (finally
         (delete-recursively! ~sym)
         (.delete ~sym)))))

(defmacro with-temp-dir
  "bindings => [name prefix-or-options ...]

  Evaluate body with names bound to java.io.File
  objects of newly created temporary directories, and a finally clause
  that deletes them recursively in reverse order.

  Prefix is a string that is used to name the directory.
  Options is a map with the keys:
  :delete-on-exit - delete the dir on JVM exit (default true)
  :dir - directory to create the dir in (default is the system temp dir).
         Must be of type that can be passed to clojure.java.io/dir.
  :prefix - a string that is used to name the directory."
  [bindings & body]
  {:pre [(vector? bindings) (even? (count bindings))]}
  (with-bindings-macro bindings body `with-temp-dir with-temp-dir-fn))
