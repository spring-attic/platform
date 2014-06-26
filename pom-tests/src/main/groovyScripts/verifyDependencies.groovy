def targetDir = new File(project.build.directory).absoluteFile
def effectivePom = new File(targetDir, "effective-pom.xml")
def platformProjectDir = new File(project.basedir, "../platform-bom").absoluteFile

def exclusions = [
	'openid4java-nodeps':['groupId':'com.google.code.guice', 'artifactId':'guice'],
	'xws-security':['groupId':'javax.xml.crypto', 'artifactId':'xmldsig']
]

println "mvn help:effective-pom -Doutput=$effectivePom".execute(null, platformProjectDir).text

def pomHeader = """
<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<project xmlns=\"http://maven.apache.org/POM/4.0.0\"
		xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
		xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">

	<modelVersion>4.0.0</modelVersion>

	<groupId>test</groupId>
	<artifactId>test</artifactId>
	<version>1.0.0.BUILD-SNAPSHOT</version>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>io.spring.platform</groupId>
				<artifactId>platform-bom</artifactId>
				<scope>import</scope>
				<type>pom</type>
				<version>$project.version</version>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<dependencies>"""

def pomFooter = """	</dependencies>
	<repositories>
		<!-- Required for GemFire and Boot snapshots -->
		<repository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots repository</name>
			<url>http://repo.spring.io/libs-snapshot</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
		<!-- Required for various Neo4J-related dependencies -->
		<repository>
			<id>opengeo</id>
			<name>OpenGeo</name>
			<url>http://repo.opengeo.org</url>
		</repository>
	</repositories>

</project>"""

new File(targetDir, "pom.xml").withWriter { writer ->
	writer.println pomHeader

	def xml = new XmlSlurper().parseText(effectivePom.text)
	xml.dependencyManagement.dependencies.dependency.each { dependency ->
		writer.println "		<dependency>"
		writer.println "			<groupId>${dependency.groupId}</groupId>"
		writer.println "			<artifactId>${dependency.artifactId}</artifactId>"
		if (dependency.type.size()) {
			writer.println "			<type>${dependency.type}</type>"
		}
		def exclusion = exclusions[dependency.artifactId.text()]
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

	writer.println pomFooter
}

def build = "mvn clean package".execute(null, targetDir)
System.out << build.in
System.err << build.err

if (build.exitValue()) {
	throw new IllegalStateException("Maven build of test project failed")
}