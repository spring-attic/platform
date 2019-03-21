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

import org.apache.maven.model.building.ModelBuilder
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.ConfigurationProperties
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.internal.impl.DefaultRepositorySystem
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.LocalRepositoryManager
import org.eclipse.aether.resolution.ArtifactDescriptorPolicy
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.spi.locator.ServiceLocator
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy


class AetherHelper {

	private final ServiceLocator serviceLocator = createServiceLocator()

	ModelBuilder createModelBuilder() {
		return this.serviceLocator.getService(ModelBuilder.class)
	}

	RepositorySystem createRepositorySystem() {
		return this.serviceLocator.getService(RepositorySystem.class)
	}

	RepositorySystemSession createRepositorySystemSession(
			RepositorySystem repositorySystem, int connectTimeout, int requestTimeout) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession()

		session.setConfigProperty(
				ConfigurationProperties.CONNECT_TIMEOUT,
				connectTimeout)
		session.setConfigProperty(
				ConfigurationProperties.REQUEST_TIMEOUT,
				requestTimeout)

		LocalRepository localRepository = new LocalRepository(getM2RepoDirectory())
		LocalRepositoryManager localRepositoryManager = repositorySystem
				.newLocalRepositoryManager(session, localRepository)
		session.setLocalRepositoryManager(localRepositoryManager)

		session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(
				ArtifactDescriptorPolicy.STRICT))

		return session
	}

	private ServiceLocator createServiceLocator() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator()
		locator.addService(RepositorySystem, DefaultRepositorySystem)
		locator.addService(RepositoryConnectorFactory,
				BasicRepositoryConnectorFactory)
		locator.addService(TransporterFactory, HttpTransporterFactory)
		locator.addService(TransporterFactory, FileTransporterFactory)
		return locator
	}

	private File getM2RepoDirectory() {
		new File(getMavenHome(), "repository")
	}

	private File getMavenHome() {
		new File(System.getProperty("user.home"), ".m2")
	}
}
