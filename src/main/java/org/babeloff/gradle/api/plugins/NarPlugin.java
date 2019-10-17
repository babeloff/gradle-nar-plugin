package org.babeloff.gradle.api.plugins;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.babeloff.gradle.api.conventions.DefaultNarPluginConvention;
import org.babeloff.gradle.api.conventions.NarPluginConvention;
import org.babeloff.gradle.api.tasks.bundlings.NarTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * <p>
 * A {@link Plugin} which extends the {@link JavaPlugin} to add tasks
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
class NarPlugin implements Plugin<Project>
{
    public static final String NAR_PLUGIN_NAME = "org.babeloff.nar-plugin";
    public static final String NAR_TASK_NAME = "narPlugin";
    public static final String NAR_IMPLEMENTATION_CONFIGURATION_NAME = "narImplementation";
    public static final String NAR_RUNTIME_ELEMENTS_CONFIGURATION_NAME = "narRuntimeElements";
    public static final String NAR_GROUP = BasePlugin.BUILD_GROUP; // could be "nifi"
    private static final Logger logger = LogManager.getLogger(NarPlugin.class);
    private final ObjectFactory objectFactory;
    private final ImmutableAttributesFactory attributesFactory;

    @Inject
    public NarPlugin(ObjectFactory objectFactory, ImmutableAttributesFactory attributesFactory)
    {
        this.objectFactory = objectFactory;
        this.attributesFactory = attributesFactory;
    }

    @Override
    public void apply(final Project project)
    {
        logger.trace("apply: entry");
        project.getPluginManager()
                .apply(JavaPlugin.class);

        final NarPluginConvention pluginConvention = project
                .getExtensions()
                .create(NAR_PLUGIN_NAME, DefaultNarPluginConvention.class);

        final TaskContainer tasks = project.getTasks();

        tasks.withType(NarTask.class).configureEach(task ->
        {
            //task.from((Callable) () -> pluginConvention.);

            task.dependsOn((Callable) () ->
            {
                return project.getConvention()
                        .getPlugin(JavaPluginConvention.class)
                        .getSourceSets()
                        .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                        .getRuntimeClasspath();
            });

            task.classpath((Callable) () ->
            {
                final FileCollection runtimeClasspath =
                        project.getConvention()
                                .getPlugin(JavaPluginConvention.class)
                                .getSourceSets()
                                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                                .getRuntimeClasspath();

                final Configuration providedRuntime =
                        project.getConfigurations()
                                .getByName(NAR_RUNTIME_ELEMENTS_CONFIGURATION_NAME);

                return runtimeClasspath.minus(providedRuntime);
            });

        });

        final TaskProvider<NarTask> nar = tasks.register(NAR_TASK_NAME, NarTask.class, (narTask1) ->
        {
            narTask1.setDescription("Generates a nar archive with all the compiled classes, per http://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#nars..");
            narTask1.setGroup(NAR_GROUP);

            //narTask1.getInputs().files(conf);
            narTask1.setProjectBuildDirectory(pluginConvention.getProjectBuildDirectory());
            narTask1.setFailOnMissingClassifierArtifact(pluginConvention.getFailOnMissingClassifierArtifact());

            logger.info("configure bundled dependencies");
//            narTask1.bundledDependencies = Arrays.asList(
//                    project.getConfigurations().getAt("runtime"),
//                    project.getTasksByName(JavaPlugin.JAR_TASK_NAME, true));
//
//            logger.info("configure parent nar manifest entry");
//            nar1.parentNarConfiguration = conf;

            final Set<Task> narTasks = project.getTasksByName(BasePlugin.ASSEMBLE_TASK_NAME, true);
            narTasks.forEach((k) -> k.dependsOn(narTask1));

        });

        final PublishArtifact narArtifact = new LazyPublishArtifact(nar);

        project.getExtensions()
                .getByType(DefaultArtifactPublicationSet.class)
                .addCandidate(narArtifact);

        configureConfigurations(project.getConfigurations());
        configureComponent(project, narArtifact);

        logger.trace("apply: exit");
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
    public void configureConfigurations(final ConfigurationContainer configurationContainer)
    {
        final Configuration narCompileConfiguration = configurationContainer
                .create(NAR_IMPLEMENTATION_CONFIGURATION_NAME)
                .setVisible(false)
                .setDescription("Additional compile classpath for libraries that should not be part of the NAR archive.");

        configurationContainer
                .getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
                .extendsFrom(narCompileConfiguration);

        final Configuration narRuntimeConfiguration = configurationContainer
                .create(NAR_RUNTIME_ELEMENTS_CONFIGURATION_NAME)
                .setVisible(false)
                .extendsFrom(narCompileConfiguration)
                .setDescription("Additional runtime classpath for libraries that should not be part of the NAR archive.");

        configurationContainer
                .getByName(JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME)
                .extendsFrom(narRuntimeConfiguration);
    }

    private void configureComponent(final Project project, final PublishArtifact narArtifact)
    {
        final AttributeContainer attributes = this
                .attributesFactory
                .mutable()
                .attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_RUNTIME));

        project.getComponents()
                .add(objectFactory.newInstance(NifiApplication.class, narArtifact, "master", attributes));
    }

}
