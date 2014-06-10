@Grab('org.yaml:snakeyaml:1.13')
import org.yaml.snakeyaml.Yaml

def getRootDir() {
	@groovy.transform.SourceURI
	def source
	new File(source).parentFile.parentFile
}

def getId(dependency) {
	"${dependency.groupId}:${dependency.artifactId}:${dependency.type}"
}

def getBootVersion() {
	def yaml = new Yaml().load(new File(rootDir, 'platform-definition.yaml').text)
	yaml['platform_definition']['projects'].find{it['name'] == 'Spring Boot'}['version']
}

def getDependenciesPom() {
	def uri = "https://repo.spring.io/libs-snapshot/org/springframework/boot/spring-boot-dependencies/$bootVersion"
	def version = bootVersion
	if (version.endsWith('-SNAPSHOT')) {
		def snapshot = new XmlSlurper().parse("$uri/maven-metadata.xml").versioning.snapshot
		version = version.replace('-SNAPSHOT', "-$snapshot.timestamp-$snapshot.buildNumber")
	}
	new XmlSlurper().parse("$uri/spring-boot-dependencies-${version}.pom")
}

def dependencies = dependenciesPom
		.dependencyManagement.dependencies.dependency
		.collect { getId(it)}

new XmlSlurper().parseText(new File(rootDir, 'platform-bom/pom.xml').text)
		.dependencyManagement.dependencies.dependency
		.collect { getId(it) }
		.findAll { dependencies.contains(it) }
		.each { println it }
