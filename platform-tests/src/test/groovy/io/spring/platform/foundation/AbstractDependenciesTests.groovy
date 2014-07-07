package io.spring.platform.foundation;

import groovy.text.SimpleTemplateEngine

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker

class AbstractDependenciesTests {

	def projectVersion = System.getProperty("project.version")

	def exclusions = [
		"openid4java-nodeps":["groupId":"com.google.code.guice", "artifactId":"guice"],
		"xws-security":["groupId":"javax.xml.crypto", "artifactId":"xmldsig"]
	]

	def eachDependency(Closure closure) {
		def xml = new XmlSlurper().parse(generateEffectivePom())
		xml.dependencyManagement.dependencies.dependency.each { dependency ->
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