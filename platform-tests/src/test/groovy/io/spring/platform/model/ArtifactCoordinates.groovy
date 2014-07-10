package io.spring.platform.model

abstract class ArtifactCoordinates {

	final String groupId

	final String artifactId

	final String version

	ArtifactCoordinates(String groupId, String artifactId, String version) {
		this.groupId = groupId
		this.artifactId = artifactId
		this.version = version
	}
}