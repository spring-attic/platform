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

package io.spring.platform.model

class Module extends ArtifactCoordinates {

	private final Set dependencies = new TreeSet( { d1, d2 ->
		def comparison = d1.groupId.compareTo(d2.groupId)
		if (!comparison) {
			comparison = d1.artifactId.compareTo(d2.artifactId)
		}
		comparison
	})

	Module(String groupId, String artifactId, String version) {
		super(groupId, artifactId, version)
	}

	boolean addDependency(Dependency dependency) {
		dependencies.add(dependency)
	}

	@Override
	String toString() {
		"$groupId:$artifactId:$version"
	}
}
