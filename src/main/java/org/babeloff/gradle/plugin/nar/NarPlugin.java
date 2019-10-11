package org.babeloff.gradle.plugin.nar;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
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
import java.util.concurrent.Callable;

/**
 * <p>
 *     A {@link Plugin} which extends the {@link JavaPlugin} to add tasks
 *     which assemble an application into a NiFi Archive (NAR) file.
 * </p>
 *
 * https://docs.gradle.org/current/userguide/custom_plugins.html
 * https://guides.gradle.org/designing-gradle-plugins/
 *
 * @author Fred Eisele
 */
class NarPlugin implements Plugin<Project>
{
    private static final Logger logger = LogManager.getLogger(NarPlugin.class);

    public static final String NAR_PLUGIN_NAME = "nar";
    public static final String NAR_TASK_NAME = "nar";
    public static final String PROVIDED_COMPILE_CONFIGURATION_NAME = "nar";
    public static final String PROVIDED_RUNTIME_CONFIGURATION_NAME = "nar";
    public static final String NAR_GROUP = BasePlugin.BUILD_GROUP; // could be "nifi"

    private final ObjectFactory objectFactory;
    private final ImmutableAttributesFactory attributesFactory;

    @Inject
    public NarPlugin(ObjectFactory objectFactory, ImmutableAttributesFactory attributesFactory)
    {
        this.objectFactory = objectFactory;
        this.attributesFactory = attributesFactory;
    }

    @Override
    public void apply(final Project project) {
        logger.trace("apply: entry");
        project.getPluginManager().apply(JavaPlugin.class);
        final NarPluginConvention pluginConvention = new DefaultNarPluginConvention(project);
        project.getConvention().getPlugins().put(NAR_PLUGIN_NAME, pluginConvention);

        final TaskContainer tasks = project.getTasks();
        tasks.withType(Nar.class).configureEach(task ->
        {
            task.from((Callable) () -> pluginConvention.getNifiAppDir());

            task.dependsOn((Callable) () ->
                    project.getConvention()
                            .getPlugin(JavaPluginConvention.class)
                            .getSourceSets()
                            .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                            .getRuntimeClasspath());

            task.classpath((Callable) () ->
            {
                final FileCollection runtimeClasspath =
                        project
                        .getConvention()
                        .getPlugin(JavaPluginConvention.class)
                        .getSourceSets()
                        .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                        .getRuntimeClasspath();

                final Configuration providedRuntime =
                        project
                        .getConfigurations()
                        .getByName(PROVIDED_RUNTIME_CONFIGURATION_NAME);

                return runtimeClasspath.minus(providedRuntime);
            });
        });

        final TaskProvider<Nar> nar = tasks.register(NAR_TASK_NAME, Nar.class, (nar1) ->
        {
            nar1.setDescription("Generates a nar archive with all the compiled classes, the web-app content and the libraries.");
            nar1.setGroup(NAR_GROUP);
        });

        PublishArtifact narArtifact = new LazyPublishArtifact(nar);
        project.getExtensions()
                .getByType(DefaultArtifactPublicationSet.class)
                .addCandidate(narArtifact);
        configureConfigurations(project.getConfigurations());
        configureComponent(project, narArtifact);
        logger.trace("apply: exit");
    }

    public void configureConfigurations(final ConfigurationContainer configurationContainer) {
        final Configuration provideCompileConfiguration = configurationContainer
                .create(PROVIDED_COMPILE_CONFIGURATION_NAME).setVisible(false)
                .setDescription("Additional compile classpath for libraries that should not be part of the NAR archive.");

        configurationContainer
                .getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
                .extendsFrom(provideCompileConfiguration);

        final Configuration provideRuntimeConfiguration = configurationContainer
                .create(PROVIDED_RUNTIME_CONFIGURATION_NAME).setVisible(false)
                .extendsFrom(provideCompileConfiguration)
                .setDescription("Additional runtime classpath for libraries that should not be part of the NAR archive.");

        configurationContainer
                .getByName(JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME)
                .extendsFrom(provideRuntimeConfiguration);
    }

    private void configureComponent(final Project project, final PublishArtifact narArtifact) {
        final AttributeContainer attributes = attributesFactory
                .mutable()
                .attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_RUNTIME));

        project.getComponents()
                .add(objectFactory.newInstance(NifiApplication.class, narArtifact, "master", attributes));
    }

//
//    private Configuration createNarConfiguration(Project project) {
//        final ConfigurationContainer configs = project.getConfigurations();
//        Configuration narConfiguration = configs.create(NAR_CONFIGURATION);
//        configs.getAt("compileOnly").extendsFrom(narConfiguration);
//        narConfiguration.setTransitive(false);
//        return narConfiguration;
//    }
//
//    private void createNarTask(Project project, Configuration conf) {
//        final TaskContainer tasks = project.getTasks();
//        final Nar nar = tasks.create(NAR_TASK_NAME, Nar.class);
//        nar.setDescription("Assembles a NiFi archive per http://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#nars.");
//        nar.setGroup(BasePlugin.BUILD_GROUP);
//        nar.getInputs().files(conf);
//
//        logger.info("configure bundled dependencies");
//        nar.bundledDependencies = Arrays.asList(
//                project.getConfigurations().getAt("runtime"),
//                project.getTasksByName(JavaPlugin.JAR_TASK_NAME, true));
//
//        logger.info("configure parent nar manifest entry");
//        nar.parentNarConfiguration = conf;
//
//        final Set<Task> narTasks = project.getTasksByName(BasePlugin.ASSEMBLE_TASK_NAME, true);
//        narTasks.forEach( (k) -> k.dependsOn(nar) );
//    }

}

