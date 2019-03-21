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

package io.spring.platform.resolution.aether

import io.spring.platform.model.Dependency
import io.spring.platform.model.DependencyScope
import io.spring.platform.model.Module
import io.spring.platform.model.Project
import io.spring.platform.resolution.DependenciesResolver

import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy
import org.eclipse.aether.resolution.ArtifactDescriptorRequest
import org.eclipse.aether.resolution.ArtifactDescriptorResult
import org.eclipse.aether.util.artifact.JavaScopes

class AetherDependenciesResolver implements DependenciesResolver {

	final RepositorySystem repositorySystem

	final RepositorySystemSession session

	final List<RemoteRepository> repositories

	AetherDependenciesResolver(RepositorySystem repositorySystem,
	RepositorySystemSession session,
	List<RepositoryDescriptor> repositoryDescriptors) {
		this.repositorySystem = repositorySystem
		this.session = session
		this.repositories = createRepositories(repositoryDescriptors)
	}

	@Override
	void resolveDependencies(Project project) {
		project.modules.each { resolveModuleDependencies(it) }
	}

	private void resolveModuleDependencies(Module module) {
		DefaultArtifact artifact = new DefaultArtifact("$module.groupId:$module.artifactId:$module.version")
		ArtifactDescriptorRequest request = new ArtifactDescriptorRequest(artifact, repositories, null)
		ArtifactDescriptorResult result = getRepositorySystem().readArtifactDescriptor(getSession(), request)
		result.dependencies.each { processDependency(it, module) }
	}

	private List<RemoteRepository> createRepositories(
			List<RepositoryDescriptor> repositoryDescriptors) {
		repositoryDescriptors.collect { createRepository(it) }
	}

	private RemoteRepository createRepository(RepositoryDescriptor descriptor) {
		def repositoryPolicy = new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_ALWAYS, RepositoryPolicy.CHECKSUM_POLICY_WARN)
		def builder = new RemoteRepository.Builder(descriptor.getId(), "default", descriptor.getUrl())
		return builder.setSnapshotPolicy(repositoryPolicy).build()
	}

	private void processDependency(
			org.eclipse.aether.graph.Dependency aetherDependency, Module module) {
		Dependency dependency = convertToDependency(aetherDependency);
		if (dependency.getScope() != DependencyScope.SYSTEM
		&& dependency.getScope() != DependencyScope.TEST) {
			module.addDependency(dependency);
		}
	}

	private Dependency convertToDependency(org.eclipse.aether.graph.Dependency aetherDependency) {
		String scopeStr = aetherDependency.isOptional() ? "optional" : aetherDependency.getScope()
		DependencyScope scope = DependencyScope.valueOf(scopeStr.toUpperCase())
		Artifact artifact = aetherDependency.artifact
		new Dependency(artifact.groupId, artifact.artifactId, artifact.version, scope)
	}

	private org.eclipse.aether.graph.Dependency createDependency(Module module) {
		new org.eclipse.aether.graph.Dependency(createArtifact(module), JavaScopes.COMPILE)
	}

	private Artifact createArtifact(Module module) {
		new DefaultArtifact(module.groupId, module.artifactId, "jar", module.version)
	}
}
