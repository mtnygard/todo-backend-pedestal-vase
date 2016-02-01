# todo-backend-pedestal-vase

An implementation of the [Todo Backend](http://www.todobackend.com/)
spec using Pedestal and Datomic.

## One-time Setup

1. Clone https://github.com/TodoBackend/todo-backend-js-spec.git locally
2. Run a Datomic transactor
2. Install the Datomic schema: `lein run -m install-schema _datomic_uri_`

## See it work

1. Start the application: `lein run-dev` \*
2. Open
   [the specs](http://www.todobackend.com/specs/index.html?http://localhost:8080/)
   to see test results.
3. Enter [localhost:8080](http://localhost:8080/) as the base URL

\* `lein run-dev` automatically detects code changes. Alternatively, you can run in production mode
with `lein run`.

## Configuration

To configure logging see config/logback.xml. By default, the app logs to stdout and logs/.
To learn more about configuring Logback, read its [documentation](http://logback.qos.ch/documentation.html).

## Vase spec

The file ```config/todo.edn``` defines the schema that Vase uses for
it's REST interface as well as for Datomic. Vase creates the Datomic
schema and generates routes that use the queries supplied in that
file.

## Building a Docker container

```sh
# With Leiningen
$ lein uberjar

$ sudo docker build .
```

## Building and Running an OSv image

This requires [Capstan](https://github.com/cloudius-systems/capstan) to be installed.

```sh
# With Leiningen
$ lein uberjar

$ capstan run [any options you want]
```

You can also modify your `Capstanfile` to perform the build step for you.

## License

Copyright © 2014 FIXME
