(ns abc
  (:refer-clojure :exclude [me])
  (:import a.b.c [d e] [f.g h])
  (:require [a :as aa] b [c d [e :as f]]))
