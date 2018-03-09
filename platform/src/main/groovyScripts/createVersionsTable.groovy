import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker

def request = new DefaultInvocationRequest()
request.pomFile = new File("${project.build.directory}/dependency/platform-bom.pom")
request.goals = ["help:effective-pom"]
new File("target").mkdirs()
def effectiveBom = new File("${project.build.directory}/effective-platform-bom.pom")
request.properties = ["output": effectiveBom.absolutePath]
def result = new DefaultInvoker().execute(request)
if (result.exitCode != 0) {
	throw new IllegalStateException(result.executionException);
}
def versions = [:]
new XmlSlurper().parseText(effectiveBom.text).dependencyManagement.dependencies.dependency
		.list()
		.sort { a, b ->
	def comparison = a.groupId.text().compareTo(b.groupId.text())
	if (!comparison) {
		comparison = a.artifactId.text().compareTo(b.artifactId.text())
	}
	comparison
}
.each { dependency ->
	versions["${dependency.groupId.text()}:${dependency.artifactId.text()}"] = dependency.version.text()
}

def targetDir = new File(project.build.directory).absoluteFile
def generatedResourcesDir = new File(targetDir, 'generated-resources')
generatedResourcesDir.mkdirs()

new File(generatedResourcesDir, 'versions-table.adoc').withWriter { out ->
	out.println '[options="header"]'
	out.println '|============================='
	out.println '| Group | Artifact | Version'
	versions.keySet().sort { a, b -> a.compareTo(b) }.each {key ->
		def (artifact, group) = key.split(':')
		out.println "| $artifact | $group | ${versions[key]}"
	}
	out.println '|============================='
}