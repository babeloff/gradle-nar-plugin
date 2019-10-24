package org.babeloff.gradle.api.plugins

import org.apache.log4j.LogManager
import org.babeloff.gradle.api.extensions.NarPluginExtension
import org.babeloff.gradle.api.tasks.NarTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

/**
 * <p>
 * A {@link Plugin}
 * which assemble an application into a NiFi Archive (NAR) file.
 * </p>
 * <p>
 * https://docs.gradle.org/current/userguide/custom_plugins.html
 * https://guides.gradle.org/designing-gradle-plugins/
 * https://docs.gradle.org/current/userguide/custom_gradle_types.html
 * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPlugin.java
 *
 * @author Fred Eisele
 */
class NarPlugin : Plugin<Project>
{
    companion object {
        val EXTENSION_NAME = "nar"
        val NAR_PLUGIN_NAME = "org.babeloff.nar-plugin"
        val NAR_TASK_NAME = "narPlugin"
        val NAR_IMPLEMENTATION_CONFIGURATION_NAME = "narImplementation"
        val NAR_RUNTIME_ELEMENTS_CONFIGURATION_NAME = "narRuntimeElements"
        val NAR_GROUP = BasePlugin.BUILD_GROUP // could be "nifi"
        private val logger = LogManager.getLogger(NarPlugin::class.java)
    }

    override fun apply( project: Project)
    {
        //project.plugins.apply(NarPluginBase::class.java)

        val extension = project
                .extensions
                .create(EXTENSION_NAME, NarPluginExtension::class.java, project)

        val tasks = project.tasks

        logger.info("apply ${NarTask::class.javaObjectType}")
        tasks.register( "nar", NarTask::class.javaObjectType) {

        }


//        tasks.withType(NarTask.class) {
//            conventionMapping.map("jvmArgs") { cargoPluginExtension.local.jvmArgs }
//            conventionMapping.map("logLevel") { cargoPluginExtension.local.logLevel }
//            conventionMapping.map("homeDir") { cargoPluginExtension.local.homeDir }
//        }
//
//                final TaskProvider<NarTask> nar =
//                tasks.register(NAR_TASK_NAME, NarTask.class, (narTask1) ->
//        {
//            narTask1.setDescription("Generates a nar archive with all the compiled classes, per http://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#nars..")
//            narTask1.setGroup(NAR_GROUP)
//
//            narTask1.getProjectBuildDirectory().set(extension.getProjectBuildDirectory())
//            narTask1.setFailOnMissingClassifierArtifact(extension.getFailOnMissingClassifierArtifact())
//
//            narTask1.getProject().
//
//        })
//
//        final PublishArtifact narArtifact = new LazyPublishArtifact(nar)
//
//        project.getExtensions()
//                .getByType(DefaultArtifactPublicationSet.class)
//                        .addCandidate(narArtifact)
//
//        configureConfigurations(project.getConfigurations())
//        configureComponent(project, narArtifact)
//
//        logger.trace("apply: exit")
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
//    public void configureConfigurations(final ConfigurationContainer configurationContainer)
//    {
//        final Configuration narCompileConfiguration = configurationContainer
//                .create(NAR_IMPLEMENTATION_CONFIGURATION_NAME)
//                .setVisible(false)
//                .setDescription("Additional compile classpath for libraries that should not be part of the NAR archive.")
//
//        final Configuration narRuntimeConfiguration = configurationContainer
//                .create(NAR_RUNTIME_ELEMENTS_CONFIGURATION_NAME)
//                .setVisible(false)
//                .extendsFrom(narCompileConfiguration)
//                .setDescription("Additional runtime classpath for libraries that should not be part of the NAR archive.")
//    }
//
//    private void configureComponent(final Project project, final PublishArtifact narArtifact)
//    {
//        final AttributeContainer attributes = this
//                .attributesFactory
//                .mutable()
//                .attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_RUNTIME))
//
//        project.getComponents()
//                .add(objectFactory.newInstance(NifiApplication.class, narArtifact, "master", attributes))
//    }

}

