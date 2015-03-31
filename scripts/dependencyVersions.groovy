@Grab('org.eclipse.aether:aether-util:1.0.2.v20150114')
import org.eclipse.aether.util.version.GenericVersionScheme

def getRootDir() {
	@groovy.transform.SourceURI
	def source
	new File(source).parentFile.parentFile
}

def getManagedDependencies() {
	File effectivePom = File.createTempFile('effective-platform-bom-', '.xml')
	"mvn help:effective-pom -Doutput=${effectivePom}".execute([], new File(rootDir, 'platform-bom')).waitFor()
	def managedDependencies = new XmlSlurper().parse(effectivePom).dependencyManagement.dependencies.dependency.list()
	effectivePom.delete()
	return managedDependencies
}

def listNewerVersions(def dependency) {
	def versionScheme = new GenericVersionScheme()
	def currentVersion = versionScheme.parseVersion(dependency.version as String)
	String groupId = dependency.groupId
	def url = "http://central.maven.org/maven2/${groupId.replace('.', '/')}/${dependency.artifactId}/maven-metadata.xml"
	try {
		def mavenMetadata = new XmlSlurper().parse(url)
		def laterVersions = mavenMetadata.versioning.versions.version.list()
				.collect { versionScheme.parseVersion(it as String) }
				.findAll { it.compareTo(currentVersion) > 0 }

		if (laterVersions) {
			println "${dependency.groupId}:${dependency.artifactId}:${dependency.version}"
			laterVersions.each { println "    ${it}" }
		}
	}
	catch (FileNotFoundException ex) {
		println "${dependency.groupId}:${dependency.artifactId}:${dependency.version}"
		println "    ${url} does not exist"
	}

}

managedDependencies.each {
	listNewerVersions(it)
}