package org.babeloff.gradle.api.plugins


import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.babeloff.gradle.api.extensions.NarPluginExtension
import org.babeloff.gradle.api.tasks.bundlings.NarTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

import javax.inject.Inject

/**
 * A [Plugin]
 * which assemble an application into a NiFi Archive (NAR) file.
 *
 * https://docs.gradle.org/current/userguide/custom_plugins.html
 * https://guides.gradle.org/designing-gradle-plugins/
 * https://docs.gradle.org/current/userguide/custom_gradle_types.html
 * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPlugin.java
 *
 * @author Fred Eisele
 */
class NarPluginBase(private val objectFactory: ObjectFactory,
                             private val attributesFactory: ImmutableAttributesFactory) : Plugin<Project>
{

    companion object {
        val NAR_PLUGIN_NAME = "org.babeloff.nar-plugin"
        val NAR_TASK_NAME = "narPlugin"
        val NAR_IMPLEMENTATION_CONFIGURATION_NAME = "narImplementation"
        val NAR_RUNTIME_ELEMENTS_CONFIGURATION_NAME = "narRuntimeElements"
        val NAR_GROUP = BasePlugin.BUILD_GROUP // could be "nifi"
        private val logger = LogManager.getLogger(NarPluginBase::class.java)
    }

    override fun apply(project: Project) {
        val extension = project
                .extensions
                .create(NAR_PLUGIN_NAME, NarPluginExtension::class.java)

        val tasks = project.tasks

        logger.trace("apply: exit")
    }

    /**
     * dependecies {
     * ...
     * narRuntimeElements (group="org.apache.nifi", name="nifi-standard-services-api-nar", version="1.9.2")
     * ...
     * }
     * Updates a configuration container with configurations.
     *
     * @param configurationContainer
     */
    fun configureConfigurations(configurationContainer: ConfigurationContainer) {
        val narCompileConfiguration = configurationContainer
                .create(NAR_IMPLEMENTATION_CONFIGURATION_NAME)
                .setVisible(false)
                .setDescription("Additional compile classpath for libraries that should not be part of the NAR archive.")

        val narRuntimeConfiguration = configurationContainer
                .create(NAR_RUNTIME_ELEMENTS_CONFIGURATION_NAME)
                .setVisible(false)
                .extendsFrom(narCompileConfiguration)
                .setDescription("Additional runtime classpath for libraries that should not be part of the NAR archive.")
    }

    private fun configureComponent(project: Project, narArtifact: PublishArtifact) {
        val attributes = this
                .attributesFactory
                .mutable()
                .attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage::class.java, Usage.JAVA_RUNTIME))

        project.components
                .add(objectFactory.newInstance(NifiApplication::class.java, narArtifact, "master", attributes))
    }

}

