package io.spring.platform;

import static org.junit.Assert.fail

import org.junit.Test
import org.yaml.snakeyaml.Yaml

public class PlatformDefinitionTests {

	@Test
	void definitionVersionsMatchBomVersions() {
		def versions = new Properties()
		new File('../platform-versions/target/generated-resources/platform-versions.properties').withInputStream{ versions.load(it) }
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
