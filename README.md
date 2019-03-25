## Spring IO Platform

Spring IO brings together the core Spring APIs into a cohesive and versioned foundational Platform
for modern applications. On top of this foundation it also provides domain-specific runtimes (DSRs)
optimized for specific application types. Please see the [Spring IO Platform website] for more
information.

## End of Life

The Platform will reach the end of its supported life on 9 April 2019. Maintenence releases of both
the Brussels and Cairo lines will continue to be published up until that time. Users of the Platform
are encouraged to start using Spring Boot's dependency management directly, either by using
`spring-boot-starter-parent` as their Maven project's parent, or by importing the
`spring-boot-dependencies` bom.

## Using the Platform

How you use the platform depends on whether you're using Maven or Gradle to build your application.

### Using the Platform with Maven

The Platform provides a pom file that you can either import into your application's pom, or use as
your application's parent pom. Please refer to the [documentation on using the Platform with Maven]
[Platform Maven docs] for more information.

### Using the Platform With Gradle

Thanks to the [dependency management plugin][], Gradle users can also make use of the Platform's
pom. Please refer to the [documentation on using the Platform with Gradle][Platform Gradle docs] for
more information.

## Contributing

Contributors to this project agree to uphold its [code of conduct][].
[Pull requests][] are welcome. Please see the [contributor guidelines][] for details.

## License
Spring IO Platform is released under version 2.0 of the [Apache License][].

[Spring IO Platform website]: https://platform.spring.io/platform/
[Platform Maven docs]: http://docs.spring.io/platform/docs/current-SNAPSHOT/reference/htmlsingle/#getting-started-using-spring-io-platform-maven
[dependency management plugin]: https://plugins.gradle.org/plugin/io.spring.dependency-management
[Platform Gradle docs]: http://docs.spring.io/platform/docs/current-SNAPSHOT/reference/htmlsingle/#getting-started-using-spring-io-platform-gradle
[code of conduct]: CODE_OF_CONDUCT.md
[Pull requests]: https://help.github.com/articles/using-pull-requests/
[contributor guidelines]: CONTRIBUTING.md
[Apache License]: https://www.apache.org/licenses/LICENSE-2.0