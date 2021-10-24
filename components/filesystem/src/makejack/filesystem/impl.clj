(ns makejack.filesystem.impl
  "File system implementation details."
  (:import
   [java.nio.file
    LinkOption]
   [java.nio.file.attribute
    FileAttribute
    PosixFilePermission]))

(def dont-follow-links (make-array LinkOption 0))
(def link-options (make-array LinkOption 0))
(def empty-file-attributes (into-array FileAttribute []))

(defn char-to-int
  [c]
  (- (int c) 48))

(def POSIX-PERMS
  {:owner  [PosixFilePermission/OWNER_EXECUTE
            PosixFilePermission/OWNER_WRITE
            PosixFilePermission/OWNER_READ]
   :group  [PosixFilePermission/GROUP_EXECUTE
            PosixFilePermission/GROUP_WRITE
            PosixFilePermission/GROUP_READ]
   :others [PosixFilePermission/OTHERS_EXECUTE
            PosixFilePermission/OTHERS_WRITE
            PosixFilePermission/OTHERS_READ]})
