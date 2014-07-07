package io.spring.platform.foundation

import java.io.File;

import groovy.text.SimpleTemplateEngine

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;
import org.junit.Test

class GradleDependenciesTests extends AbstractDependenciesTests {

	@Test
	public void allManagedDependenciesCanBeResolved() throws Exception {
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
				} else {
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
		GradleConnector gradleConnector = GradleConnector.newConnector();
		((DefaultGradleConnector) gradleConnector).embedded(true);
		def gradle = gradleConnector.forProjectDirectory(testDir).connect();
		gradle.newBuild().forTasks("clean", "build").run()
	}
}
