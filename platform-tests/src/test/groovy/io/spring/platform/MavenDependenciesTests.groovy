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

package io.spring.platform;

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
	void allManagedDependenciesCanBeResolved() throws Exception {
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
		def result = new DefaultInvoker().execute(request)
		if (result.executionException) {
			throw result.executionException
		} else if(result.exitCode != 0) {
			throw new IllegalStateException('Maven build execution failed')
		}
	}
}
