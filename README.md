## Spring IO Platform

Spring IO brings together the core Spring APIs into a cohesive and versioned foundational Platform
for modern applications. On top of this foundation it also provides domain-specific runtimes (DSRs)
optimized for specific application types. Please see the [Spring IO Platform website] for more
information.

## Using the Platform

How you use the platform depends on whether you're using Maven or Gradle to build your application.

### Using the Platform with Maven

The Platform provides a pom file that you can either import into your application's pom, or use as
your application's parent pom. Please refer to the [documentation on using the Platform with Maven]
[Platform Maven docs] for more information.

### Using the Platform With Gradle

The Platform provides a properties file that lists versions of all of the Platform's components and
their dependencies. This properties file can be used in conjuction with
[Spring Boot's Gradle plugin][] to provide dependency versions. Please refer to the [documentation
on using the Platform with Gradle][Platform Gradle docs] for more information.

## License
Spring IO Platform is released under version 2.0 of the [Apache License][].

[Spring IO Platform website]: http://spring.io/platform
[Spring Boot's starter parent]: http://docs.spring.io/spring-boot/docs/1.1.4.RELEASE/reference/html/using-boot-build-systems.html#using-boot-maven-parent-pom
[Spring Boot's Maven plugin]: http://docs.spring.io/spring-boot/docs/1.1.4.RELEASE/reference/html/build-tool-plugins-maven-plugin.html
[Spring Boot's Gradle plugin]: http://docs.spring.io/spring-boot/docs/1.1.4.RELEASE/reference/html/build-tool-plugins-gradle-plugin.html
[Platform Maven docs]: http://docs.spring.io/platform/docs/current-SNAPSHOT/reference/htmlsingle/#getting-started-using-spring-io-platform-maven
[Platform Gradle docs]: http://docs.spring.io/platform/docs/current-SNAPSHOT/reference/htmlsingle/#getting-started-using-spring-io-platform-gradle
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0