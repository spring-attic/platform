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

import groovy.text.SimpleTemplateEngine

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker

class AbstractDependenciesTests {

	def projectVersion = System.getProperty("project.version")

	def exclusions = [
		"openid4java-nodeps":["groupId":"com.google.code.guice", "artifactId":"guice"],
		"xws-security":["groupId":"javax.xml.crypto", "artifactId":"xmldsig"],
		"mariadb-java-client":["groupId":"net.java.dev.jna", "artifactId":"jna"]
	]

	def eachDependency(Closure closure) {
		def xml = new XmlSlurper().parse(generateEffectivePom())
		xml.dependencyManagement.dependencies.dependency
			.findAll { it.type.text() != 'test-jar'}
			.each { dependency ->
				def exclusion = exclusions[dependency.artifactId.text()]
				closure.call([dependency, exclusion])
			}
	}

	File generateEffectivePom() {
		def request = new DefaultInvocationRequest()
		request.setPomFile(new File("target/dependency/platform-bom.pom"))
		request.setGoals(["help:effective-pom"])
		request.setProperties(["output" : "effective-pom.xml"] as Properties)

		new DefaultInvoker().execute(request);

		return new File("target/dependency/effective-pom.xml")
	}

	String templateText(String templateName) {
		def model = ["projectVersion":projectVersion]
		return new SimpleTemplateEngine()
		.createTemplate(new File("src/test/resources/${templateName}.template"))
		.make(model).toString()
	}
}