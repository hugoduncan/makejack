(ns makejack.tasks.core
  "Tasks and utilities for tools.build.

  Behaviour is controlled via a params map, that is typically passed from
  the clojure CLI invocation via a -T argument.

  The params have the following defaults:

  :target  \"target\"
  :verbose true

  The :lib and :version keys are populated from `project.edn` if
  present, else must be manually supplied."
  )
