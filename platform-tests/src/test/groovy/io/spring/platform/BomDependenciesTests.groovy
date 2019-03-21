/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.platform

import io.spring.platform.model.ArtifactCoordinates
import io.spring.platform.model.Module
import org.eclipse.aether.util.version.GenericVersionScheme
import org.eclipse.aether.version.Version
import org.junit.Test

import static org.junit.Assert.fail
/**
 * Tests to verify that the bom contains the correct dependencies.
 *
 * @author Andy Wilkinson
 */
class BomDependenciesTests extends AbstractProjectAnalysisTests {

	@Test
	void dependenciesAreCorrect() {

		def ignoredDependencies = yaml['platform_definition']['missing_dependencies']['ignored_dependencies']

		def downgradedDependencies = yaml['platform_definition']['downgraded_dependencies']

		def ignoredModulesMatchers = yaml['platform_definition']['missing_dependencies']['ignored_modules'].collect {
			new ArtifactCoordinatesMatcher(it)
		}

		def unusedDependenciesMatchers = yaml['platform_definition']['unused_dependencies']['ignored'].collect {
			new ArtifactCoordinatesMatcher(it)
		}

		def versionScheme = new GenericVersionScheme()

		def analysisResult = new AnalysisResult()

		analyzeProjects { project ->
			project.modules.each { module ->
				if (!isIgnored(module, ignoredModulesMatchers)) {
					def identifier = dependencyMappings.getMappedIdentifier(module)
					if (!platformArtifacts.containsKey(identifier)) {
						analysisResult.registerMissingArtifact(identifier, module.version, null)
					}
					module.dependencies.each { dependency ->
						identifier = dependencyMappings.getMappedIdentifier(dependency)
						if (!platformArtifacts.containsKey(identifier)) {
							if (!isIgnored(module, dependency, ignoredDependencies)) {
								analysisResult.registerMissingArtifact(identifier, dependency.version, module)
							}
						}
						else if (!identifier.startsWith('org.springframework')) {
							def dependencyVersion = versionScheme.parseVersion(dependency.version)
							def platformVersion = versionScheme.parseVersion(platformArtifacts[identifier])

							if (dependencyVersion.compareTo(platformVersion) > 0) {
								String latestAllowedVersion = downgradedDependencies ? downgradedDependencies[identifier]: null
								if (!latestAllowedVersion || versionScheme.parseVersion(latestAllowedVersion).compareTo(dependencyVersion) < 0) {
									analysisResult.registerDowngradedDependency(identifier, dependencyVersion, platformVersion, module)
								}
							}
						}
						if (isIgnored(dependency, unusedDependenciesMatchers)) {
							analysisResult.registerUsedArtifact(identifier, module)
						}
					}
				}
			}
		}

		if (analysisResult.missingArtifacts || analysisResult.usedArtifacts || analysisResult.downgradedDependencies) {
			def writer = new StringWriter()
			if (analysisResult.missingArtifacts) {
				writer.append("Missing dependencies:\n")
				analysisResult.writeMissing(new PrintWriter(writer))
			}
			if (analysisResult.usedArtifacts) {
				writer.append("Used dependencies:\n")
				analysisResult.writeUsed(new PrintWriter(writer));
			}
			if (analysisResult.downgradedDependencies) {
				writer.append("Downgraded dependencies:\n")
				analysisResult.writeDowngraded(new PrintWriter(writer))
			}
			fail(writer.toString())
		}

	}

	boolean isIgnored(ArtifactCoordinates module, ArtifactCoordinates dependency, Map ignoredDependencies) {
		String moduleId = "${module.groupId}:${module.artifactId}"
		String dependencyId = "${dependency.groupId}:${dependency.artifactId}"
		ignoredDependencies[moduleId]?.contains(dependencyId)
	}

	boolean isIgnored(ArtifactCoordinates coordinates, List<ArtifactCoordinatesMatcher> matchers) {
		for (def matcher: matchers) {
			if (matcher.matches(coordinates)) {
				return true
			}
		}
		return false
	}

	class AnalysisResult {

		Map missingArtifacts = [:]

		Map artifactVersions = [:]

		Map usedArtifacts = [:]

		Map downgradedDependencies = [:]

		void registerMissingArtifact(String identifier, String version, Module user) {
			List modules = this.missingArtifacts.get(identifier, [])
			if (user != null) {
				modules.add(user)
			}
			registerVersionOfMissingArtifact(identifier, version)
		}

		void registerUsedArtifact(String identifier, Module user) {
			List users = usedArtifacts.get(identifier, [])
			users << user
		}

		void registerDowngradedDependency(String identifier, Version dependencyVersion, Version platformVersion, Module user) {
			List users = this.downgradedDependencies.get(identifier, [])
			users << "$user wants $dependencyVersion but Platform provides $platformVersion"
		}

		private void registerVersionOfMissingArtifact(String identifier, String version) {
			Set versions = this.artifactVersions.get(identifier, [] as Set)
			versions.add(version)
		}


		void writeMissing(PrintWriter writer) {
			this.missingArtifacts.each { key, value ->
				writer.println "$key ${artifactVersions[key]}"
				value.each { writer.println "    $it" }
			}
		}

		void writeUsed(PrintWriter writer) {
			this.usedArtifacts.each { id, users ->
				writer.println "    $id: $users"
			}
		}

		void writeDowngraded(PrintWriter writer) {
			this.downgradedDependencies.each { id, users ->
				writer.println "$id:"
				users.each { writer.println "    $it" }
			}
		}
	}
}