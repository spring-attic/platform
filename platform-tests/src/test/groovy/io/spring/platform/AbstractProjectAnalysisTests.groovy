/*
 * Copyright 2014-2018 the original author or authors.
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

import io.spring.platform.model.Module
import io.spring.platform.model.Project
import io.spring.platform.resolution.DependenciesResolver
import io.spring.platform.resolution.aether.AetherDependenciesResolver
import io.spring.platform.resolution.aether.AetherHelper
import io.spring.platform.resolution.aether.RepositoryDescriptor

import org.yaml.snakeyaml.Yaml

class AbstractProjectAnalysisTests {

	def yaml = new File('../platform-definition.yaml').withReader { new Yaml().load(it) }

	def dependencyMappings = new DependencyMappings(mappings: this.yaml['platform_definition']['dependency_mappings'])

	Map<String, String> platformArtifacts = new EffectivePlatformBom().managedVersions()

	void analyzeProjects(Closure analyzer) {

		def projects = createProjects()
		def resolver = createDependenciesResolver()

		projects.each { project ->
			resolver.resolveDependencies(project)
			analyzer.call(project)
		}
	}

	private DependenciesResolver createDependenciesResolver() {
		def aetherHelper = new AetherHelper()

		def repositorySystem = aetherHelper.createRepositorySystem()
		def session = aetherHelper.createRepositorySystemSession(repositorySystem, 5000, 30000)

		def repositories = createRepositories()

		new AetherDependenciesResolver(repositorySystem, session, repositories)
	}

	private List<RepositoryDescriptor> createRepositories() {
		this.yaml['platform_definition']['repositories'].collect { new RepositoryDescriptor(it['id'], it['url']) }
	}

	private List<Project> createProjects() {
		this.yaml['platform_definition']['projects'].collect { project ->
			def groupId = project['groupId']
			def version = project['version']

			def modules = project['modules'].collect { module ->
				new Module(groupId, module, version)
			}
			new Project(project['name'], project['groupId'], project['version'], modules);
		}
	}

}
