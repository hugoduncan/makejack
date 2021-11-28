# makejack

> A little like make, and jack of all tasks.

Build and maintain your clojure projects.

## Status

Early alpha. No releases at the moment.


## Intro

Makejack is a clojure CLI tool, a babashka script, and a library for
writing you own build tasks.

Start your project using the built in tasks, and as your needs grow,
easily customise the tasks to make them your own.

You specify project name and version on the command line, or put them in
a `project.edn` file.

``` clojure
{:name my.org/my-project
 :version "0.0.1"}
```

With a `project.edn` file, the build targets should work without
passing any agruments.


## Install as a Babashka script

If you have babashka 0.6.5 or greater,

``` shell
wget https://raw.githubusercontent.com/hugoduncan/makejack/main/mj
chmod +x mj
```

## Install as a Named Clojure CLI Tool

You can install makejack as a named tool:

``` shell
clj -Ttools install io.github.hugoduncan/makejack \
'{:git/sha "c6ca7ffdb207da8d78744bb849c0d889e5b81cd4"
:deps/root "projects/makejack-jar"}' \
 :as mj
```

It can then be called as:

``` shell
clj -Tmj help
```

For example, build a jar with:

``` shell
clj -Tmj jar
```


## Install as a library

You can add makejack to your `deps.edn`, wherever you configure your
build tools.

```clojure
org.hugoduncan/makejack
{:git/tag "v0.1.5" :git/sha "49f0357" :deps/root "projects/makejack-jar"}
```

### `makejack.target-doc`

Use doc strings and meta on your build tasks to provide a `help` command
for your build.

### `makejack.defaults`

Provide defaults for filesystem layouts, filenames, etc.

### `makejack.project-data`

Manage the project build data map, possibly in a project.edn file.

The :name is a qualified symbol, like my.org/project-name.

The :version is a dotted string, and can contain a computed component,
specified as a :keyword.  Currently :git-rev-count and :reverse-date are
supported.

### `makejack.poly`

Build helpers for polylith (like) monorepo projects.


# Development

## Polylith

<img src="logo.png" width="30%" alt="Polylith" id="logo">

The Polylith documentation can be found here:

- The [high-level documentation](https://polylith.gitbook.io/polylith)
- The [Polylith Tool documentation](https://github.com/polyfy/polylith)
