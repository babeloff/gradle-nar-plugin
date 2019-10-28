package org.babeloff.gradle.api.plugins

import org.apache.log4j.LogManager
import org.babeloff.gradle.api.extensions.NifiPluginExtension
import org.babeloff.gradle.api.tasks.NifiTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

/**
 * <p>
 * A {@link Plugin}
 * which assemble an application into a NiFi Archive (NIFI) file.
 * </p>
 * <p>
 * https://docs.gradle.org/current/userguide/custom_plugins.html
 * https://guides.gradle.org/designing-gradle-plugins/
 * https://docs.gradle.org/current/userguide/custom_gradle_types.html
 * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPlugin.java
 *
 * @author Fred Eisele
 */
class NifiPlugin : Plugin<Project>
{
    companion object {
        val EXTENSION_NAME = "nifi"
        val NIFI_PLUGIN_NAME = "org.babeloff.nifi-plugin"
        val NIFI_TASK_NAME = "nifiPlugin"
        val NIFI_IMPLEMENTATION_CONFIGURATION_NAME = "nifiImplementation"
        val NIFI_RUNTIME_ELEMENTS_CONFIGURATION_NAME = "nifiRuntimeElements"
        val NIFI_GROUP = BasePlugin.BUILD_GROUP // could be "nifi"
        private val logger = LogManager.getLogger(NifiPlugin::class.java)
    }

    override fun apply( project: Project)
    {
        //project.plugins.apply(NifiPluginBase::class.java)

        val extension = project
                .extensions
                .create(EXTENSION_NAME, NifiPluginExtension::class.java, project)

        val tasks = project.tasks

        logger.info("apply ${NifiTask::class.javaObjectType}")
        tasks.register( "nifi", NifiTask::class.javaObjectType) {

        }


//        tasks.withType(NifiTask.class) {
//            conventionMapping.map("jvmArgs") { cargoPluginExtension.local.jvmArgs }
//            conventionMapping.map("logLevel") { cargoPluginExtension.local.logLevel }
//            conventionMapping.map("homeDir") { cargoPluginExtension.local.homeDir }
//        }
//
//                final TaskProvider<NifiTask> nifi =
//                tasks.register(NIFI_TASK_NAME, NifiTask.class, (nifiTask1) ->
//        {
//            nifiTask1.setDescription("Generates a nifi archive with all the compiled classes, per http://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#nifis..")
//            nifiTask1.setGroup(NIFI_GROUP)
//
//            nifiTask1.getProjectBuildDirectory().set(extension.getProjectBuildDirectory())
//            nifiTask1.setFailOnMissingClassifierArtifact(extension.getFailOnMissingClassifierArtifact())
//
//            nifiTask1.getProject().
//
//        })
//
//        final PublishArtifact nifiArtifact = new LazyPublishArtifact(nifi)
//
//        project.getExtensions()
//                .getByType(DefaultArtifactPublicationSet.class)
//                        .addCandidate(nifiArtifact)
//
//        configureConfigurations(project.getConfigurations())
//        configureComponent(project, nifiArtifact)
//
//        logger.trace("apply: exit")
    }

    /**
     * dependecies {
     * ...
     * nifiRuntimeElements (group="org.apache.nifi", name="nifi-standard-services-api-nifi", version="1.9.2")
     * ...
     * }
     * Updates a configuration container with configurations.
     *
     * @param configurationContainer
     */
//    public void configureConfigurations(final ConfigurationContainer configurationContainer)
//    {
//        final Configuration nifiCompileConfiguration = configurationContainer
//                .create(NIFI_IMPLEMENTATION_CONFIGURATION_NAME)
//                .setVisible(false)
//                .setDescription("Additional compile classpath for libraries that should not be part of the NIFI archive.")
//
//        final Configuration nifiRuntimeConfiguration = configurationContainer
//                .create(NIFI_RUNTIME_ELEMENTS_CONFIGURATION_NAME)
//                .setVisible(false)
//                .extendsFrom(nifiCompileConfiguration)
//                .setDescription("Additional runtime classpath for libraries that should not be part of the NIFI archive.")
//    }
//
//    private void configureComponent(final Project project, final PublishArtifact nifiArtifact)
//    {
//        final AttributeContainer attributes = this
//                .attributesFactory
//                .mutable()
//                .attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_RUNTIME))
//
//        project.getComponents()
//                .add(objectFactory.newInstance(NifiApplication.class, nifiArtifact, "master", attributes))
//    }

}

