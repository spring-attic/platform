/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.platform

import io.spring.platform.model.ArtifactCoordinates

class ArtifactCoordinatesMatcher {

	private static final String WILDCARD = "*"

	private final String groupSpec

	private final String artifactSpec

	private final String versionSpec

	public ArtifactCoordinatesMatcher(String spec) {
		String[] components = spec.split(":")
		if (components.length == 1) {
			this.groupSpec = components[0]
			this.artifactSpec = WILDCARD
			this.versionSpec = WILDCARD
		}
		else if (components.length == 2) {
			this.groupSpec = components[0]
			this.artifactSpec = components[1]
			this.versionSpec = WILDCARD
		}
		else if (components.length == 3) {
			this.groupSpec = components[0]
			this.artifactSpec = components[1]
			this.versionSpec = components[2]
		}
		else {
			throw new IllegalArgumentException(
			"The spec may have a maximum of three colon-separated sections");
		}
	}

	boolean matches(ArtifactCoordinates artifactCoordinates) {
		boolean matches = true
		matches = matchesSpec(artifactCoordinates.getGroupId(), this.groupSpec)
		if (matches) {
			matches = matchesSpec(artifactCoordinates.getArtifactId(), this.artifactSpec)
			if (matches) {
				matches = matchesSpec(artifactCoordinates.getVersion(), this.versionSpec)
			}
		}
		matches
	}

	private boolean matchesSpec(String candidate, String spec) {
		if (WILDCARD == spec) {
			true
		}
		else if (spec.endsWith(WILDCARD)) {
			candidate.startsWith(spec.substring(0, spec.length() - WILDCARD.length()))
		}
		else {
			spec == candidate
		}
	}

	@Override
	String toString() {
		return "$groupSpec:$artifactSpec:$versionSpec"
	}

}
