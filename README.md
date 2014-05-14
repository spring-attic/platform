## Spring IO Platform

Spring IO brings together the core Spring APIs into a cohesive and versioned foundational Platform
for modern applications. On top of this foundation it also provides domain-specific runtimes (DSRs)
optimized for specific application types. See the Spring IO website for
[more information][platform website].

## Using the Platform

How you use the platform depends on whether you're using Maven or Gradle to build your applications.

### Using the Platform with Maven

The Platform provides a pom file that you can use as your application's parent pom:

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

	<modelVersion>4.0.0</modelVersion>

	<groupId>com.example</groupId>
	<artifactId>your-application</artifactId>
	<version>1.0.0-SNAPSHOT</version>

	<parent>
		<groupId>io.spring.platform</groupId>
		<artifactId>platform</artifactId>
		<version>1.0.0.BUILD-SNAPSHOT</version>
	</parent>

	…

</project>
```

This parent pom makes the versions of the Platform's components and their dependencies available to your
application. Via its inheritance of [Spring Boot's starter parent][]
it also configures [Spring Boot's Maven plugin][].

If you prefer to use your own parent pom, you can still make use of the Platform's versions by importing its pom instead:

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

	<modelVersion>4.0.0</modelVersion>

	<groupId>com.example</groupId>
	<artifactId>your-application</artifactId>
	<version>1.0.0-SNAPSHOT</version>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>io.spring.platform</groupId>
				<artifactId>platform</artifactId>
				<version>1.0.0.BUILD-SNAPSHOT</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	…

</project>
```

### Using the Platform With Gradle

The Platform provides a properties file that lists versions of all of the Platform's components and their dependencies.
This properties file can be used in conjuction with [Spring Boot's Gradle plugin][] to provide dependency versions by
declaring a dependency upon it in the `versionManagement` configuration:

```
buildscript {
    dependencies {
        classpath 'org.springframework.boot:spring-boot-gradle-plugin:1.1.0.M1'
    }
}

apply plugin: 'spring-boot'

dependencies {
	versionManagement = 'io.spring.platform:platform-versions:1.0.0.BUILD-SNAPSHOT@properties'
}
…
```

## License
Spring IO Platform is released under version 2.0 of the [Apache License][].

[platform website]: http://spring.io/platform
[Spring Boot's starter parent]: http://docs.spring.io/spring-boot/docs/1.1.0.M1/reference/html/using-boot-build-systems.html#using-boot-maven-parent-pom
[Spring Boot's Maven plugin]: http://docs.spring.io/spring-boot/docs/1.1.0.M1/reference/html/build-tool-plugins-maven-plugin.html
[Spring Boot's Gradle plugin]: http://docs.spring.io/spring-boot/docs/1.1.0.M1/reference/html/build-tool-plugins-gradle-plugin.html
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
