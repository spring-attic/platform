def targetDir = new File(project.build.outputDirectory).absoluteFile
def effectivePom = new File(targetDir, "effective-pom.xml")
def platformProjectDir = new File(project.basedir, "../platform").absoluteFile

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
				<artifactId>platform</artifactId>
				<scope>import</scope>
				<type>pom</type>
				<version>1.0.0.BUILD-SNAPSHOT</version>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<dependencies>"""

def pomFooter = """    </dependencies>
	<repositories>
		<repository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>http://repo.spring.io/libs-snapshot</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
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
		if (dependency.artifactId == 'openid4java-nodeps') {
			writer.println """			<exclusions>
				<exclusion>
					<groupId>com.google.code.guice</groupId>
					<artifactId>guice</artifactId>
				</exclusion>
			</exclusions>"""
		}
		writer.println "		</dependency>"
	}

	writer.println pomFooter
}

def build = "mvn clean package".execute(null, targetDir)
build.in.eachLine { println it }

if (build.exitValue()) {
	throw new IllegalStateException("Maven build of test project failed")
}