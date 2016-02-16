/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.platform

import static org.junit.Assert.fail
import io.spring.platform.model.ArtifactCoordinates
import io.spring.platform.model.Module

import org.junit.Test

/**
 * Tests to verify that the bom contains the correct dependencies.
 *
 * @author Andy Wilkinson
 */
class BomDependenciesTests extends AbstractProjectAnalysisTests {

	@Test
	void dependenciesAreCorrect() {

		def ignoredDependencies = yaml['platform_definition']['missing_dependencies']['ignored_dependencies']

		def ignoredModulesMatchers = yaml['platform_definition']['missing_dependencies']['ignored_modules'].collect {
			new ArtifactCoordinatesMatcher(it)
		}

		def unusedDependenciesMatchers = yaml['platform_definition']['unused_dependencies']['ignored'].collect {
			new ArtifactCoordinatesMatcher(it)
		}

		def analysisResult = new AnalysisResult()

		analyzeProjects { project ->
			project.modules.each { module ->
				if (!isIgnored(module, ignoredModulesMatchers)) {
					def identifier = dependencyMappings.getMappedIdentifier(module)
					if (!platformArtifacts.contains(identifier)) {
						analysisResult.registerMissingArtifact(identifier, module.version, null)
					}
					module.dependencies.each { dependency ->
						identifier = dependencyMappings.getMappedIdentifier(dependency)
						if (!platformArtifacts.contains(identifier)) {
							if (!isIgnored(module, dependency, ignoredDependencies)) {
								analysisResult.registerMissingArtifact(identifier, dependency.version, module)
							}
						}
						if (isIgnored(dependency, unusedDependenciesMatchers)) {
							analysisResult.registerUsedArtifact(identifier)
						}
					}
				}
			}
		}

		if (analysisResult.missingArtifacts || analysisResult.usedArtifacts) {
			def writer = new StringWriter()
			if (analysisResult.missingArtifacts) {
				writer.append("Missing dependencies:\n")
				analysisResult.writeMissing(new PrintWriter(writer))
			}
			if (analysisResult.usedArtifacts) {
				writer.append("Used dependencies:\n")
				analysisResult.writeUsed(new PrintWriter(writer));
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

		List usedArtifacts = []

		void registerMissingArtifact(String identifier, String version, Module user) {
			List modules = this.missingArtifacts.get(identifier, [])
			if (user != null) {
				modules.add(user)
			}
			registerVersionOfMissingArtifact(identifier, version)
		}

		void registerUsedArtifact(String identifier) {
			usedArtifacts << identifier
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
			this.usedArtifacts.each { id ->
				writer.println "    $id"
			}
		}
	}
}