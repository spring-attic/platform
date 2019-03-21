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

class Project {

	final String name

	final String groupId

	final String version

	final List<Module> modules

	Project(String name, String groupId, String version, List<Module> modules) {
		this.name = name
		this.groupId = groupId
		this.version = version
		this.modules = modules.sort { m1, m2 -> m1.artifactId.compareTo(m2.artifactId) }
	}
}
