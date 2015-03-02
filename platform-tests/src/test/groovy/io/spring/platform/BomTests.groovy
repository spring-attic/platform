package io.spring.platform.foundation

import org.junit.Test

import static org.junit.Assert.fail

public class BomTests {

	@Test
	void dependencyManagementIsNotDuplicated() {
		def dependencies = extractDependenciesFromPom(bootDependenciesPom)
		def duplicateDependencies = extractDependenciesFromPom(platformBom)
				.findAll { dependencies.contains(it) }

		if (duplicateDependencies) {
			String message = "The following dependencies are duplicates:"
			duplicateDependencies.each {
				message += "\n    $it"
			}
			fail(message)
		}
	}

	@Test
	void versionPropertiesAreNotDuplicated() {
		def versions = extractVersionsFromPom(bootDependenciesPom)
		def duplicateVersions = extractVersionsFromPom(platformBom)
				.findAll { key, value -> versions.containsKey(key) && versions[key] == value}

		if (duplicateVersions) {
			String message = "The following versions are duplicates:"
			duplicateVersions.each {
				message += "\n    $it"
			}
			fail(message)
		}
	}

	@Test
	void versionPropertiesAreNotOverridden() {
		def expectedOverrides = [ 'junit.version', 'jetty.version', 'servlet-api.version',
				'tomcat.version' ]
		def versions = extractVersionsFromPom(bootDependenciesPom)

		def overriddenVersions = [:]
		extractVersionsFromPom(platformBom)
				.findAll { key, value -> versions.containsKey(key) && versions[key] != value }
				.each { overriddenVersions[it.key] = "${versions[it.key]} -> ${it.value}" }

		def missingOverrides = expectedOverrides.findAll { !overriddenVersions.containsKey(it) }

		expectedOverrides.each { overriddenVersions.remove it }

		String message = ''
		if (overriddenVersions) {
			message += "The following versions are overridden:"
			overriddenVersions.each {
				message += "\n    $it"
			}
		}

		if (missingOverrides) {
			if (message) {
				message += '\n'
			}
			message += "The following versions were not overridden:"
			missingOverrides.each {
				message += "\n    $it"
			}
		}

		if (message.length() > 0) {
			fail(message)
		}
	}


	def getBootDependenciesPom() {
		def uri = "https://repo.spring.io/libs-snapshot/org/springframework/boot/spring-boot-dependencies/$bootVersion"
		String version = bootVersion
		if (version.endsWith('-SNAPSHOT')) {
			def snapshot = new XmlSlurper().parse("$uri/maven-metadata.xml").versioning.snapshot
			version = version.replace('-SNAPSHOT', "-$snapshot.timestamp-$snapshot.buildNumber")
		}
		new URL("$uri/spring-boot-dependencies-${version}.pom").text
	}

	def getPlatformBom() {
		new File('target/dependency/platform-bom.pom').text
	}

	String getBootVersion() {
		new XmlSlurper()
			.parse(new File('target/dependency/platform-bom.pom'))
			.parent.version.text()
	}

	def extractDependenciesFromPom(def pom) {
		def dependencies = new XmlSlurper().parseText(pom)
			.dependencyManagement.dependencies.dependency
			.collect { getIdForDependency(it)}
	}

	def getIdForDependency(dependency) {
		"${dependency.groupId}:${dependency.artifactId}"
	}

	Map extractVersionsFromPom(def pom) {
		Map versions = [:]
		new XmlParser().parseText(pom)
			.properties[0].children()
			.findAll { it.name().localPart.endsWith('.version') }
			.each { versions[it.name().localPart] = it.text() }
		versions
	}
}
