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

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker

class EffectivePlatformBom {

    def dependencyManagement

    EffectivePlatformBom() {
        def request = new DefaultInvocationRequest()
        def effectiveBom = new File("target/effective-platform-bom.pom")
        if (!effectiveBom.exists()) {
            def pomFile = new File("target/dependency/platform-bom.pom")
            request.pomFile = pomFile
            request.goals = ["help:effective-pom"]
            request.properties = ["output": effectiveBom.absolutePath]
            new DefaultInvoker().execute(request)
        }
        dependencyManagement = new XmlSlurper().parseText(effectiveBom.text).dependencyManagement
    }

    Map<String, String> managedVersions() {
        def versions = [:]
        dependencyManagement.dependencies.dependency
                .list()
                .sort { a, b ->
            def comparison = a.groupId.text().compareTo(b.groupId.text())
            if (!comparison) {
                comparison = a.artifactId.text().compareTo(b.artifactId.text())
            }
            comparison
        }
        .each { dependency ->
            versions["${dependency.groupId.text()}:${dependency.artifactId.text()}"] = dependency.version.text()
        }
        return versions
    }

}
