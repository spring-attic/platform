def targetDir = new File(project.build.directory).absoluteFile

def versionProperties = new Properties()

new File(targetDir, 'dependency/platform-versions.properties').withInputStream { versionProperties.load(it) }

def generatedResourcesDir = new File(targetDir, 'generated-resources')
generatedResourcesDir.mkdirs()

new File(generatedResourcesDir, 'versions-table.adoc').withWriter { out ->
	out.println '[options="header"]'
	out.println '|============================='
	out.println '| Group | Artifact | Version |'
	versionProperties.keySet().sort { a, b -> a.compareTo(b) }.each {key -> 
		def (artifact, group) = key.split(':')
		out.println "| $artifact | $group | ${versionProperties[key]} |"
	}
	out.println '|============================='
}