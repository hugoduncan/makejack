# makejack

Build your clojure projects.

Makejack is a clojure CLI tool for building clojure projects, and a
library for writing you own build tasks.

Start your project using the built in tasks, and as your needs grow,
customise the tasks with your own build targets.

# Install as a Named Clojure CLI Tool

You can install makejack as a named tool:

``` shell
clj -Ttools install io.github.hugoduncan/makejack \
'{:git/sha "9676516" :deps/root "projects/makejack-jar"}' \
 :as mj
```

It can then be called as:

``` shell
clj -Tmj help
```

Create a `project.edn` file, and the build targets should work.

``` clojure
{:name my.org/my-project
 :version "0.0.1"}
```

For example, build a jar with:

``` shell
clj -Tmj jar
```


# Install as a library

You can add makejack to your `deps.edn`, wherever you configure your
build tools.

```clojure
org.hugoduncan/makejack {:mvn/vesion "tbd"}
```

## `makejack.target-doc`

Use doc strings and meta on your build tasks to provide a `help` command
for your build.

## `makejack.defaults`

Provide defaults for filesystem layouts, filenames, etc.

## `makejack.project-data`

Manage the project build data map, possibly in a project.edn file.

The :name is a qualified symbol, like my.org/project-name.

The :version is a dotted string, and can contain a computed component,
specified as a :keyword.  Currently :git-rev-count and :reverse-date are
supported.

## `makejack.poly`

Build helpers for polylith (like) monorepo projects.

## `makejack.path` and `makejack.filesystem`

Helpers for manipulating paths and the filesystem.


# Development

## Polylith

<img src="logo.png" width="30%" alt="Polylith" id="logo">

The Polylith documentation can be found here:

- The [high-level documentation](https://polylith.gitbook.io/polylith)
- The [Polylith Tool documentation](https://github.com/polyfy/polylith)
