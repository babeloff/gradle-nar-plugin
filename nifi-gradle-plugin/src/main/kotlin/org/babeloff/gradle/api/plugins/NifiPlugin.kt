package org.babeloff.gradle.api.plugins

import org.apache.log4j.LogManager
import org.babeloff.gradle.api.extensions.NifiPluginExtension
import org.babeloff.gradle.api.tasks.NifiTask
import org.babeloff.gradle.api.tasks.NifiTask.Companion.DEPENDENCIES_FILE
import org.babeloff.gradle.api.tasks.NifiTask.Companion.LICENSE_FILE
import org.babeloff.gradle.api.tasks.NifiTask.Companion.NOTICE_FILE
import org.babeloff.gradle.api.tasks.NifiTask.Companion.SERVICES_DIRECTORY
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.get
import java.io.File
import java.time.LocalDateTime

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

        // ensure java plugin is applied
        if (project.plugins.hasPlugin(JavaPlugin::class.java)) {
            project.plugins.apply(JavaPlugin::class.java)
        }

        // create the nifi configurations
        NifiImplementation(project.configurations)

        // create an instance of the nifi-nar task
        val tasks = project.tasks

        logger.info("apply ${NifiTask::class.javaObjectType}")
        val nar = tasks.register( "nifi-nar", NifiTask::class.javaObjectType) {
            dependsOn("jar")
        }
        nar.configure {
            val rcp = project.configurations.get("runtimeClasspath")
            dependsOn(rcp)

            into("META-INF/bundled-dependencies") {
                from ({ rcp.filter { it.name.endsWith("jar") }.map { it } } )
            }

            into(SERVICES_DIRECTORY) { from("src/main/resources/services") }

            description = "Assembles a Nifi archive containing the main classes jar and the runtime configuration dependencies"
            inputDir.set(File(project.buildDir.canonicalFile, "libs").canonicalFile)
            includeList.set(NifiTask.DEFAULT_INCLUDES)
            excludeList.set(NifiTask.DEFAULT_EXCLUDES)
            finalName.set(project.name)

            defaultManifestFiles.set(File(project.buildDir.canonicalFile, "META-INF/MANIFEST.MF").canonicalFile)
            useDefaultManifestFile.set(false)
            forceCreation.set(false)
            archiveExtension.set(NifiTask.NIFI_EXTENSION)

            failOnMissingClassifierArtifact.set(true)

            includeTypes.set("")
            excludeTypes.set("")

            includeScope.set("")
            excludeScope.set("")

            includeClassifiers.set("")
            excludeClassifiers.set("")

            copyDepClassifier.set("")
            type.set(EXTENSION_NAME)

            includeArtifactIds.set("")
            excludeArtifactIds.set("")

            includeGroupIds.set("")
            excludeGroupIds.set("")

            markersDirectory.set(File(project.buildDir, "dependency-maven-plugin-markers").canonicalFile)

            overWriteReleases.set(true)
            overWriteSnapshots.set(true)
            overWriteIfNewer.set(true)
            projectBuildDirectory.set(project.buildDir.canonicalFile)

            reactorProjects.set("")
            silent.set(false)
            outputAbsoluteArtifactFilename.set(false)

            logger.debug("initializing nifi task nifi")
            nifiGroup.set(project.group.toString())
            nifiId.set(project.name)
            nifiVersion.set(project.version.toString())

            /**
             * Nifi archives can have a parent nifi. The parent is optional and there can be at maximum one parent.
             * The parent relationship is added to the manifest.
             * To tell the plugin to add a parent you have to add a nifi dependency to the nifi configuration.
             * The nifi configuration is created by the plugin.
             */
            logger.debug("initializing nifi task dependency")
            nifiDependency.set(false)
            nifiDependencyGroup.set("org.apache.nifi")
            nifiDependencyId.set("nifi-standard-services-api-nifi")
            nifiDependencyVersion.set("0.2.1")

            logger.debug("initializing nifi task build info")
            buildTag.set("release")
            buildBranch.set("master")
            buildRevision.set(NifiTask.VERSION_FORMAT.format(LocalDateTime.now()))

            cloneDuringInstanceClassLoading.set(false)
            enforceDocGeneration.set(false)


            into(DEPENDENCIES_FILE) {
                from()
            }
            into(LICENSE_FILE) {
                from()
            }
            into(NOTICE_FILE) {
                from()
            }
        }

        /**
         * Set the task properties with plugin exensions
         */
//        val extension = project
//                .extensions
//                .create(EXTENSION_NAME, NifiPluginExtension::class.java, project)

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
    private fun NifiImplementation(container: ConfigurationContainer)
    {
        val nifiCompileConfiguration = container.create(NIFI_IMPLEMENTATION_CONFIGURATION_NAME)
        //container.getByName("implementation").extendsFrom(nifiCompileConfiguration)

        nifiCompileConfiguration.isTransitive = false
        nifiCompileConfiguration.isVisible = false
        nifiCompileConfiguration.description = "Additional compile classpath for libraries that should not be part of the NIFI archive."

        val nifiRuntimeConfiguration = container.create(NIFI_RUNTIME_ELEMENTS_CONFIGURATION_NAME)
        nifiRuntimeConfiguration.extendsFrom(nifiCompileConfiguration)
        nifiRuntimeConfiguration.isTransitive = false
        nifiRuntimeConfiguration.isVisible = false
        nifiRuntimeConfiguration.description = "Additional runtime classpath for libraries that should not be part of the NIFI archive."

    }

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

