/*
 * Copyright 2014 the original author or authors.
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

import org.junit.Test

import static org.junit.Assert.fail

class UnusedDependenciesTests extends AbstractProjectAnalysisTests {

	def ignoredArtifacts = this.yaml['platform_definition']['unused_dependencies']['ignored']

	@Test
	void thereAreNoUnusedDependencies() {
		def unusedArtifacts = new HashSet(platformArtifacts.keySet());

		this.analyzeProjects { project ->
			project.modules.each { module ->
				unusedArtifacts.remove("$module.groupId:$module.artifactId" as String)
				module.dependencies.each { dependency ->
					unusedArtifacts.remove(dependencyMappings.getMappedIdentifier(dependency))
				}
			}
		}

		unusedArtifacts.removeAll(this.ignoredArtifacts)

		if (unusedArtifacts) {
			def message = "Unused dependencies: "
			unusedArtifacts.each { message += "\n    $it" }
			fail(message)
		}
	}
}