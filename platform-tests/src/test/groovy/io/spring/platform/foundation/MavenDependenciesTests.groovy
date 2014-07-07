package io.spring.platform.foundation;

import groovy.text.SimpleTemplateEngine;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class MavenDependenciesTests extends AbstractDependenciesTests {

	@Test
	public void allManagedDependenciesCanBeResolved() throws Exception {
		def testDir = new File("target/maven-test")
		testDir.mkdirs()

		new File(testDir, "pom.xml").withWriter { writer ->

			writer.println templateText('pom-header')

			eachDependency { dependency, exclusion ->
				writer.println "		<dependency>"
				writer.println "			<groupId>${dependency.groupId}</groupId>"
				writer.println "			<artifactId>${dependency.artifactId}</artifactId>"
				if (dependency.type.size()) {
					writer.println "			<type>${dependency.type}</type>"
				}
				if (exclusion) {
					writer.println """			<exclusions>
						<exclusion>
							<groupId>${exclusion.groupId}</groupId>
							<artifactId>${exclusion.artifactId}</artifactId>
						</exclusion>
					</exclusions>"""
				}
				writer.println "		</dependency>"
			}

			writer.println templateText('pom-footer')
		}

		def request = new DefaultInvocationRequest()
		request.setBaseDirectory(testDir)
		request.setGoals(["package"])
		def result = new DefaultInvoker().execute(request);
		if (result.executionException) {
			throw result.executionException
		} else if(result.exitCode != 0) {
			throw new IllegalStateException('Maven build execution failed')
		}
	}
}
