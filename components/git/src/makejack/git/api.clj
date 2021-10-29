(ns makejack.git.api
  "Git command line tasks"
  (:require [makejack.git.impl :as impl]))


(defn tag
  "Shells out to git and tag a commit-like (commit-sha, tag, etc)
    git tag HEAD <tag>

  Return the command output.

  Options:
    :dir - dir to invoke this command from, by default current directory
    :commit - the commit to tag, e.g a sha, HEAD by default
    :tag - tag tp add"
  {:arglists '[[{:keys [commit dir tag]}]]}
  [params]
  (impl/tag params))
