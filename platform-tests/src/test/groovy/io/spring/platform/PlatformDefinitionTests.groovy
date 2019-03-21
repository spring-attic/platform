/*
 * Copyright 2014-2018 the original author or authors.
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

import static org.junit.Assert.fail

import org.junit.Test
import org.yaml.snakeyaml.Yaml

public class PlatformDefinitionTests {

	@Test
	void definitionVersionsMatchBomVersions() {
		def versions = new EffectivePlatformBom().managedVersions()
		def yaml = new Yaml().load(new File('../platform-definition.yaml').text)
		def incorrectVersions = [:]
		yaml['platform_definition']['projects'].each { project ->
			def version = project['version']
			def group = project['groupId']

			for (def module: project['modules']) {
				def id = "$group:$module" as String
				if (versions[id] && versions[id] != version) {
					incorrectVersions[project['name']] = versions[id]
				}
			}
		}
		if (!incorrectVersions.isEmpty()) {
			String message = "The following projects require version updates:"
			incorrectVersions.each { key, value ->
				message += "\n\t$key to $value"
			}
			fail(message)
		}
	}

}
