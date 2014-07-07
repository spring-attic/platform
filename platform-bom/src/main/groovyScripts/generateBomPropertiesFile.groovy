def targetDir = new File(project.build.directory).absoluteFile
def effectivePom = new File(targetDir, "effective-pom.xml")

new File(targetDir, "platform-bom.properties").withWriter { writer ->
	def xml = new XmlSlurper().parseText(effectivePom.text)
	xml.project.find {it.artifactId.text() == 'platform-bom'}.dependencyManagement.dependencies.dependency
		.list()
		.sort { a, b ->
			def comparison = a.groupId.text().compareTo(b.groupId.text())
			if (!comparison) {
				comparison = a.artifactId.text().compareTo(b.artifactId.text())
			}
			comparison
		}
		.each { dependency ->
			writer.println "${dependency.groupId}\\:${dependency.artifactId}=${dependency.version}"
		}
}