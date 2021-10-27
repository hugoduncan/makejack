# makejack

Build tooling.


## Project data

Makejack builds on top of the philosophy of build as code, but
recognises that there is project data that doesn't influence how the
project built, just artifact names, and metadata.

`makejack.project-data` allows you to manage this data, and possibly
store it in a `project.edn` file.

It provides several ways of constructing your project version, either as
a literal, or as a template containing `:git-rev-count` or
`:reverse-date` components.

## Development

### Polylith

<img src="logo.png" width="30%" alt="Polylith" id="logo">

The Polylith documentation can be found here:

- The [high-level documentation](https://polylith.gitbook.io/polylith)
- The [Polylith Tool documentation](https://github.com/polyfy/polylith)
