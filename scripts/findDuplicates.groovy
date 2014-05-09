def getId(dependency) {
	"${dependency.groupId}:${dependency.artifactId}:${dependency.type}"
}

def dependencies = []

new XmlSlurper().parseText(new File(args[0]).text)
		.dependencyManagement.dependencies.dependency
		.each { dependencies << getId(it) }

new XmlSlurper().parseText(new File(args[1]).text)
		.dependencyManagement.dependencies.dependency
		.collect { getId(it) }
		.findAll { dependencies.contains(it) }
		.each { println it }