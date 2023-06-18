# jproto

jproto is a prototype of HashLisp implemented in Java 17.

All Java 17 syntax and libraries can be used.

## Building

The build system is gradle.
Build output is placed in `build/`.

Gradle supports a `--info` option for more verbose output.

* **Run tests:**

    `./gradlew test`

    *NB. Does not produce very verbose console output.*

    Test reports: `build/reports/tests/test/index.html`

    Force test rerunning: `./gradlew test --rerun`

* **Build installable:**

    `./gradlew build`

    Output is: `build/distributions/jproto.tar`

* **Run demo:**

    `./gradlew run`

* **'Install' and run:**

    `./gradlew installDist`

    `build/install/jproto/bin/jproto [args...]`

* **Javadocs:**

    `./gradlew javadoc`

    Output is: `build/docs/javadoc/index.html`

You can combine targets on the command line, for example `installDist` doesn't run tests but `build` does:

```
./gradlew build installDist && build/install/jproto/bin/jproto example.lisp
```

### Build profile reporting

You can get a profile report of the build without using the gradle scan service.

```
./gradlew --profile clean test build run installDist
```

This leaves output in: `build/reports/profile/`.
