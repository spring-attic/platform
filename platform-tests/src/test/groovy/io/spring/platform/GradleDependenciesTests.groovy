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

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import org.junit.After
import org.junit.Before
import org.junit.Test

class GradleDependenciesTests extends AbstractDependenciesTests {

	private String projectVersion;

	@Before
	public void clearProjectVersionSystemProperty() {
		this.projectVersion = System.clearProperty("project.version")
	}

	@After
	public void restoreProjectVersionSystemProperty() {
		System.setProperty("project.version", this.projectVersion)
	}

	@Test
	void allManagedDependenciesCanBeResolved() throws Exception {
		def testDir = new File("target/gradle-test")
		testDir.mkdirs()

		new File(testDir, "build.gradle").withWriter { writer ->
			writer.println templateText('gradle-header')

			eachDependency { dependency, exclusion ->
				if (dependency.type.size()) {
					if ('test-jar' == dependency.type.text()) {
						writer.print("    compile (group: '${dependency.groupId}', name: '${dependency.artifactId}', classifier: 'tests')")
					} else {
						writer.print("    compile ('${dependency.groupId}:${dependency.artifactId}@${dependency.type}')")
					}
				} else if (dependency.classifier.size()) {
					writer.print("    compile (group: '${dependency.groupId}', name: '${dependency.artifactId}', classifier: '${dependency.classifier}')")
				}
				else {
					writer.print("    compile ('${dependency.groupId}:${dependency.artifactId}')")
				}
				if (exclusion) {
					writer.println ' {'
					writer.println "        exclude group: '${exclusion.groupId}', module: '${exclusion.artifactId}'"
					writer.println '    }'
				} else {
					writer.println ''
				}
			}

			writer.println templateText('gradle-footer')
		}
		GradleConnector gradleConnector = GradleConnector.newConnector()
		((DefaultGradleConnector) gradleConnector).embedded(true)
		def gradle = gradleConnector.forProjectDirectory(testDir).connect()
		gradle.newBuild().setStandardOutput(System.out).setStandardError(System.out).forTasks("resolveDependencies").withArguments("--refresh-dependencies").run()
	}
}
