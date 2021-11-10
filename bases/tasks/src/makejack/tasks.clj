(ns makejack.tasks
  "Makejack's tasks.

  Use this namespace directly, or use them in your own build namespace.

  `require` is a helper to easily pull in multiple tasks into a
  namespace."
  (:refer-clojure :exclude [require])
  (:require
   [clojure.string :as str]
   [makejack.tasks.impl :as impl]))

(defmacro require
  "Require makejack tasks.

  Inspired by `require`, but for tasks.

  Takes task specs as arguments.  A tasks spec is either a task symbol,
  or a vector, with task symbol as the first element.  The vector can
  contain an alias, using `:as`, and a default argument map, with
  `:default`.

    (tasks/require
       help
       [clean :as make-it-shiny]
       [jar :defaults {:name 'my/project :version \"0.1.0\"}])"
  [& target-specs]
  `(do
     ~@(for [spec target-specs]
         (impl/wrap-one spec))))

(defn ^{:doc-order 0 :params "[:target target-name]"} help
  "Show help

  Use :target to get detailed help on a specific target."
  [params]
  ;; TODO: make this a function in target-doc
  ((requiring-resolve 'makejack.tasks.help/help)
   (merge {:ns 'makejack.tasks} params)))

(defn clean
  "Remove all built files"
  [params]
  ((requiring-resolve 'makejack.tasks.clean/clean)
   params))

(defn bump-version
  "Bump the project version at a given level."
  [params]
  ((requiring-resolve 'makejack.tasks.bump-version/bump-version)
   params))

(defn jar
  "Build a jar file"
  [params]
  ((requiring-resolve 'makejack.tasks.jar/jar)
   params))

(defn uber
  "Build a uberjar file"
  [params]
  ((requiring-resolve 'makejack.tasks.uber/uber)
   params))

(defn install
  "install jar to local maven repository."
  [params]
  ((requiring-resolve 'makejack.tasks.install/install)
   params))

(defn tag-version
  "Add a git tag with the latest version tag."
  [params]
  ((requiring-resolve 'makejack.tasks.tag-version/tag-version)
   params))

(defn clj-kondo
  "Run clj-kondo.
  When the :init keyword is true, then intialise with all dependencies."
  [params]
  ((requiring-resolve 'makejack.tasks.clj-kondo/clj-kondo)
   params))

(defn ns-tree
  "Return namespace tree info."
  [params]
  ((requiring-resolve 'makejack.tasks.ns-tree/ns-tree)
   params))

(defn- aliases-spec-to-aliases
  [spec]
  (cond
    (keyword? spec) [spec]
    (string? spec)  (mapv keyword (str/split spec #"\."))
    :else           spec))

(defn- normalise-aliases [params]
  (cond-> params
    (:aliases params)
    (update :aliases aliases-spec-to-aliases)))

(defn compile-clj
  "AOT compile clojure namespaces"
  [params]
  ((requiring-resolve 'makejack.tasks.compile-clj/compile-clj)
   (normalise-aliases params)))

(defn javac
  "javaac compile java classes"
  [params]
  ((requiring-resolve 'makejack.tasks.javac/javac)
   (normalise-aliases params)))

(defn poly
  "Run a mj task on a polylith project.

  Unrecognised options are passed to the target.

  Options:
    - :on     as per `poly test`, e.g :project
    - :target  the mj target to invoke."
  [params]
  ((requiring-resolve 'makejack.tasks.poly/poly)
   (normalise-aliases params)))

(defn poly-clj-kondo
  "Run clj-kondo over a polylith project.
  Assumes a .clj-kondo config at the polylith root.
  When the :init keyword is true, then intialise with all dependencies."
  [params]
  ((requiring-resolve 'makejack.tasks.poly-clj-kondo/poly-clj-kondo)
   params))

(defn poly-main
  "clojure -M in namespaces"
  [params]
  ((requiring-resolve 'makejack.tasks.poly-clj-cli/poly-main)
   (normalise-aliases params)))

(defn poly-exec
  "clojure -X in namespaces"
  [params]
  ((requiring-resolve 'makejack.tasks.poly-clj-cli/poly-exec)
   (normalise-aliases params)))

(defn poly-tool
  "clojure -T in namespaces"
  [params]
  ((requiring-resolve 'makejack.tasks.poly-clj-cli/poly-tool)
   (normalise-aliases params)))

(defn changelog-init
  "Intialise a changelog"
  [params]
  ((requiring-resolve 'makejack.tasks.changelog/init)
   params))

(defn changelog-release
  "Update the changelog for a release."
  [params]
  ((requiring-resolve 'makejack.tasks.changelog/release)
   params))
