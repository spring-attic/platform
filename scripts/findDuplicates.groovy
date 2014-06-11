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
	new URL("$uri/spring-boot-dependencies-${version}.pom").text
}

def extractDependenciesFromPom(def pom) {
	new XmlSlurper().parseText(pom)
		.dependencyManagement.dependencies.dependency
		.collect { getId(it)}
}

def extractVersionsFromPom(def pom) {
	new XmlParser().parseText(pom)
		.properties[0].children()
		.collect {it.name().localPart}
		.findAll{it.endsWith('.version')}
}

def dependenciesPom = getDependenciesPom()
def platformBom = new File(rootDir, 'platform-bom/pom.xml').text

def dependencies = extractDependenciesFromPom(dependenciesPom)
def duplicateDependencies = extractDependenciesFromPom(platformBom)
		.findAll { dependencies.contains(it) }

if (duplicateDependencies) {
	println "Duplicate dependencies found:"
	duplicateDependencies.each { println "    $it"}
}

def versions = extractVersionsFromPom(dependenciesPom)
def duplicateVersions = extractVersionsFromPom(platformBom)
		.findAll {versions.contains(it)}

if (duplicateVersions) {
	println "Duplicate versions found:"
	duplicateVersions.each { println "    $it"}
}