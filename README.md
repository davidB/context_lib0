[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# Overview

A Set of libraries (tools) to help manage Context and its propagation (implicit) into a Java or Scala application.

# Build

* build localy
    ```
    ./gradlew assemble
    ````
* publish to local maven repository
    ```
    ./gradlew publishToMavenLocal
    ````
* release
    ```
    git tag -a "${version}" -m "release"
    git push --tags
    ```

  [ci-img]: https://travis-ci.org/davidB/context_lib0.svg?branch=master
  [ci]: https://travis-ci.org/davidB/context_lib0
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/java-span-reporter.svg?maxAge=2592000
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Cjava-span-reporter
