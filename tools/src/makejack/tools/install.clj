(ns makejack.tools.install
  "Install to local repository"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [makejack.api.filesystem :as filesystem]
            [makejack.api.path :as path]
            [makejack.api.tool :as tool]
            [makejack.api.util :as util])
  (:import [org.apache.maven.artifact.repository.metadata Metadata Versioning]
           [org.apache.maven.artifact.repository.metadata.io.xpp3
            MetadataXpp3Reader MetadataXpp3Writer]))

(defn paths-to-install
  [mj project]
  (let [target-path (:target-path mj)
        version     (:version project)
        artifact-id (:artifact-id project)]
    [{:source-path (path/path "pom.xml")
      :target-path (str artifact-id "-" version ".pom")}
     {:source-path (path/path target-path (:jar-name project))
      :target-path (:jar-name project)}]))

(defn prepare-path-install! [path-map dir]
  (let [source-path (:source-path path-map)
        target-path (path/path dir (:target-path path-map))]
    (filesystem/mkdirs dir)
    (filesystem/copy-file! source-path target-path {:replace-existing true})
    (let [{:keys [md5 sha1]} (util/file-hashes source-path)]
      (spit (.toFile (path/path-with-extension target-path ".md5")) md5)
      (spit (.toFile (path/path-with-extension target-path ".sha1")) sha1))))

(defn group-path [project]
  (apply path/path (str/split (:group-id project) #"\.")))

(defn generate-metadata-model
  [project dir]
  (filesystem/mkdirs dir)
  (let [path               (path/path dir "maven-metadata-local.xml")
        ^Metadata metadata (if (filesystem/file-exists? path)
                             (with-open [^java.io.InputStream in (io/input-stream
                                                                  (path/as-file path))]
                               (.read (MetadataXpp3Reader.) in))
                             (Metadata.))
        writer             (MetadataXpp3Writer.)]
    (.setGroupId metadata (:group-id project))
    (.setArtifactId metadata (:artifact-id project))
    (.setFileComment writer "Written by Makejack")
    (let [^Versioning versioning (or (.getVersioning metadata) (Versioning.))]
      (.addVersion versioning (:version project))
      (.updateTimestamp versioning))
    (with-open [^java.io.OutputStream out (io/output-stream
                                           (path/as-file
                                            (path/path dir "maven-metadata.xml")))]
      (.write writer out metadata))))

(defn install
  "Install a project to a maven repository."
  [_options _args {:keys [mj project] :as _config}]
  (let [paths         (paths-to-install mj project)
        repo-home     (path/path (System/getProperty "user.home") ".m2" "repository")
        artifact-path (path/path repo-home (group-path project) (:name project))
        version-path  (path/path artifact-path (:version project))]
    (doseq [path paths]
      (prepare-path-install! path version-path))
    (generate-metadata-model project artifact-path)
    nil))

(def extra-options
  [["-a" "--aliases ALIASES" "Aliases to use."
    :parse-fn tool/parse-kw-stringlist]])

(defn -main [& args]
  (tool/dispatch-main "install" "[options]" install extra-options args)
  (shutdown-agents))
