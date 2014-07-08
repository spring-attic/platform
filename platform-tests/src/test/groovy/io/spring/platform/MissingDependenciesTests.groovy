/*
 * Copyright 2014 the original author or authors.
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

class MissingDependenciesTests extends AbstractProjectAnalysisTests {

	@Test
	void thereAreNoMissingDependencies() {

		def ignoredDependenciesMatchers = yaml['platform_definition']['missing_dependencies']['ignored_dependencies'].collect {
			new ArtifactCoordinatesMatcher(it)
		}

		def ignoredModulesMatchers = yaml['platform_definition']['missing_dependencies']['ignored_modules'].collect {
			new ArtifactCoordinatesMatcher(it)
		}

		def analysisResult = new AnalysisResult()

		analyzeProjects { project ->
			project.modules.each { module ->
				if (!isIgnored(module, ignoredModulesMatchers)) {
					def identifier = dependencyMappings.getMappedIdentifier(module)
					if (!platformArtifacts.contains(identifier) && !isIgnored(module, ignoredDependenciesMatchers)) {
						analysisResult.registerMissingArtifact(identifier, module.version, null)
					}
					module.dependencies.each { dependency ->
						identifier = dependencyMappings.getMappedIdentifier(dependency)
						if (!platformArtifacts.contains(identifier) && !isIgnored(dependency, ignoredDependenciesMatchers)) {
							analysisResult.registerMissingArtifact(identifier, dependency.version, module)
						}
					}
				}
			}
		}

		if (analysisResult.missingArtifacts) {
			def writer = new StringWriter()
			analysisResult.write(new PrintWriter(writer))
			fail("Missing dependencies:\n${writer.toString()}")
		}
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

		void registerMissingArtifact(String identifier, String version, Module user) {
			List modules = this.missingArtifacts.get(identifier, [])
			if (user != null) {
				modules.add(user)
			}
			registerVersionOfMissingArtifact(identifier, version)
		}

		private void registerVersionOfMissingArtifact(String identifier, String version) {
			Set versions = this.artifactVersions.get(identifier, [] as Set)
			versions.add(version)
		}

		void write(PrintWriter writer) {
			this.missingArtifacts.each { key, value ->
				writer.println "$key ${artifactVersions[key]}"
				value.each { writer.println "    $it" }
			}
		}
	}
}