package org.babeloff.gradle.api.tasks.bundlings;


import groovy.lang.Closure;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.DependencyStatusSets;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.filters.DestFileFilter;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.resolvers.ArtifactsResolver;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.resolvers.DefaultArtifactsResolver;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.translators.ArtifactTranslator;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.translators.ClassifierTypeTranslator;
import org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection.*;
import org.gradle.internal.impldep.org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.gradle.internal.impldep.org.apache.nifi.extension.definition.ExtensionDefinition;
import org.gradle.internal.impldep.org.apache.nifi.extension.definition.ExtensionType;
import org.gradle.internal.impldep.org.apache.nifi.extension.definition.ServiceAPIDefinition;
import org.gradle.internal.impldep.org.apache.nifi.extension.definition.extraction.ExtensionClassLoader;
import org.gradle.internal.impldep.org.apache.nifi.extension.definition.extraction.ExtensionClassLoaderFactory;
import org.gradle.internal.impldep.org.apache.nifi.extension.definition.extraction.ExtensionDefinitionFactory;
import org.gradle.internal.impldep.org.apache.nifi.extension.definition.extraction.StandardServiceAPIDefinition;
import org.babeloff.gradle.api.conventions.MavenArchiveConfiguration;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.api.artifacts.dsl.ArtifactHandler;
import org.gradle.api.file.*;
import org.gradle.api.internal.file.copy.DefaultCopySpec;
import org.gradle.api.java.archives.internal.DefaultManifest;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.Optional;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;
import org.gradle.internal.impldep.org.apache.maven.artifact.factory.ArtifactFactory;
import org.gradle.internal.impldep.org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.gradle.internal.impldep.org.apache.maven.artifact.installer.ArtifactInstaller;
import org.gradle.internal.impldep.org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactCollector;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactResolver;
import org.gradle.internal.impldep.org.apache.maven.plugin.logging.Log;
import org.gradle.internal.impldep.org.apache.maven.project.MavenProjectHelper;
import org.gradle.internal.impldep.org.apache.maven.project.ProjectBuilder;
import org.gradle.internal.impldep.org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource;

import org.gradle.util.ConfigureUtil;
import org.gradle.api.tasks.bundling.Jar;

import javax.annotation.Nullable;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Assembles a NAR archive.
 *
 * @author Fred Eisele
 */
public class NarTask extends Jar {
    private static final Logger logger = LogManager.getLogger(NarTask.class);

    public static final String NAR_EXTENSION = "nar";

    private File nifiXml;
    private FileCollection classpath;
    private final DefaultCopySpec metaInf;

    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String BUNDLE_DIRECTORY = "META-INF/bundled-dependencies/";
    private static final String DOCUMENTATION_WRITER_CLASS_NAME = "org.apache.nifi.documentation.xml.XmlDocumentationWriter";

    private static final String[] DEFAULT_EXCLUDES = new String[]{"**/package.html"};
    private static final String[] DEFAULT_INCLUDES = new String[]{"**/**"};

    private static final String BUILD_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";


    // BOOLEAN Inputs
    @Input
    public Property<Boolean> getEnforceDocGeneration() {
        return this.enforceDocGeneration;
    }
    private Property<Boolean> enforceDocGeneration = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<Boolean> getUseDefaultManifestFile() {
        return this.useDefaultManifestFile;
    }
    private Property<Boolean> useDefaultManifestFile = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<Boolean> getForceCreation() {
        return this.forceCreation;
    }
    private Property<Boolean> forceCreation = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<Boolean> getFailOnMissingClassifierArtifact() {
        return this.failOnMissingClassifierArtifact;
    }
    public void setFailOnMissingClassifierArtifact(Boolean value) { this.failOnMissingClassifierArtifact.convention(value); }
    private Property<Boolean> failOnMissingClassifierArtifact = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<Boolean> getOverWriteReleases() {
        return this.overWriteReleases;
    }
    private Property<Boolean> overWriteReleases = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<Boolean> getOverWriteSnapshots() {
        return this.overWriteSnapshots;
    }
    private Property<Boolean> overWriteSnapshots = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<Boolean> getOverWriteIfNewer() {
        return this.overWriteIfNewer;
    }
    private Property<Boolean> overWriteIfNewer = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<Boolean> getSilent() {
        return this.silent;
    }
    private Property<Boolean> silent = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<Boolean> getOutputAbsoluteArtifactFilename() {
        return this.outputAbsoluteArtifactFilename;
    }
    private Property<Boolean> outputAbsoluteArtifactFilename = getProject().getObjects().property(Boolean.class);
    @Input
    public Property<Boolean> getCloneDuringInstanceClassLoading() {
        return this.cloneDuringInstanceClassLoading;
    }
    private Property<Boolean> cloneDuringInstanceClassLoading = getProject().getObjects().property(Boolean.class);

    // STRING Inputs
    @Input
    public Property<String> getArchiveClassifier() { return this.classifier;  }
    private Property<String> classifier = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getIncludeTypes() {
        return this.includeTypes;
    }
    private Property<String> includeTypes = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getExcludeTypes() {
        return this.excludeTypes;
    }
    private Property<String> excludeTypes = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getIncludeScope() {
        return this.includeScope;
    }
    private Property<String> includeScope = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getExcludeScope() {
        return this.excludeScope;
    }
    private Property<String> excludeScope = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getIncludeClassifiers() {
        return this.includeClassifiers;
    }
    private Property<String> includeClassifiers = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getExcludeClassifiers() {
        return this.excludeClassifiers;
    }
    private Property<String> excludeClassifiers = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getCopyDepClassifier() {
        return this.copyDepClassifier;
    }
    private Property<String> copyDepClassifier = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getType() {
        return this.type;
    }
    private Property<String> type = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getExcludeArtifactIds() {
        return this.excludeArtifactIds;
    }
    private Property<String> excludeArtifactIds = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getIncludeArtifactIds() {
        return this.includeArtifactIds;
    }
    private Property<String> includeArtifactIds = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getExcludeGroupIds() {
        return this.excludeGroupIds;
    }
    private Property<String> excludeGroupIds = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getIncludeGroupIds() {
        return this.includeGroupIds;
    }
    private Property<String> includeGroupIds = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getNarDependencyGroup() {
        return this.narDependencyGroup;
    }
    private Property<String> narDependencyGroup = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getNarDependencyId() {
        return this.narDependencyId;
    }
    private Property<String> narDependencyId = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getNarDependencyVersion() {
        return this.narDependencyVersion;
    }
    private Property<String> narDependencyVersion = getProject().getObjects().property(String.class);


    @Input
    public Property<String> getFinalName() {
        return this.finalName;
    }
    private Property<String> finalName = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getNarGroup() {
        return this.narGroup;
    }
    private Property<String> narGroup = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getNarId() {
        return this.narId;
    }
    private Property<String> narId = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getNarVersion() {
        return this.narVersion;
    }
    private Property<String> narVersion = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getBuildTag() {
        return this.buildTag;
    }
    private Property<String> buildTag = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getBuildBranch() {
        return this.buildBranch;
    }
    private Property<String> buildBranch = getProject().getObjects().property(String.class);
    @Input
    public Property<String> getBuildRevision() {
        return this.buildRevision;
    }
    private Property<String> buildRevision = getProject().getObjects().property(String.class);


    // LIST of STRING Inputs
    @Input
    public ListProperty<String> getIncludeList() { return this.includes; }
    public ListProperty<String> includes = getProject().getObjects().listProperty(String.class);
    @Input
    public ListProperty<String> getExcludeList() { return this.excludes; }
    public ListProperty<String> excludes = getProject().getObjects().listProperty(String.class);


    // FILESYSTEM Inputs
    @OutputFile
    public RegularFileProperty getDefaultManifestFiles() { return this.defaultManifestFiles; }
    public RegularFileProperty defaultManifestFiles = getProject().getObjects().fileProperty();
    @OutputDirectory
    public DirectoryProperty getProjectBuildDirectory() { return this.projectBuildDirectory; }
    public void setProjectBuildDirectory(Directory value) { this.projectBuildDirectory.convention(value); }
    private DirectoryProperty projectBuildDirectory = getProject().getObjects().directoryProperty();
    @OutputDirectory
    public DirectoryProperty getMarkersDirectory() { return this.markersDirectory; }
    public DirectoryProperty markersDirectory = getProject().getObjects().directoryProperty();

    @Input
    public ListProperty<String> getReactorProjects() {
        return this.reactorProjects;
    }
    public ListProperty<String> reactorProjects = getProject().getObjects().listProperty(String.class);


    // QUASI-COMPONENTS Inputs
    @Input
    public Property<ArchiverManager> getArchiverManager() {
        return this.archiverManager;
    }
    public Property<ArchiverManager> archiverManager = getProject().getObjects().property(ArchiverManager.class);

    @Input
    public Property<Project> getProjects() {
        return this.project;
    }
    public Property<Project> project = getProject().getObjects().property(Project.class);

    @Input
    public Property<ArtifactRepositoryContainer> getRepositories() {
        return this.repositories;
    }
    public Property<ArtifactRepositoryContainer> repositories = getProject().getObjects().property(ArtifactRepositoryContainer.class);

    @Input
    public Property<MavenArchiveConfiguration> getArchive() {
        return this.archive;
    }
    public Property<MavenArchiveConfiguration> archive = getProject().getObjects().property(MavenArchiveConfiguration.class);


    // COMPONENTS

    @Input
    public Property<JarArchiver> getJarArchiver() {
        return this.jarArchiver;
    }
    public Property<JarArchiver> jarArchiver = getProject().getObjects().property(JarArchiver.class);
    @Input
    public Property<MavenProjectHelper> getProjectHelper() {
        return this.projectHelper;
    }
    public Property<MavenProjectHelper> projectHelper = getProject().getObjects().property(MavenProjectHelper.class);
    @Input
    public Property<ArtifactInstaller> getInstaller() {
        return this.installer;
    }
    public Property<ArtifactInstaller> installer = getProject().getObjects().property(ArtifactInstaller.class);
    @Input
    public Property<ArtifactRepositoryFactory> getRepositoryFactory() {
        return this.repositoryFactory;
    }
    public Property<ArtifactRepositoryFactory> repositoryFactory = getProject().getObjects().property(ArtifactRepositoryFactory.class);
    @Input
    public Property<ArtifactFactory> getFactory() {
        return this.factory;
    }
    public Property<ArtifactFactory> factory = getProject().getObjects().property(ArtifactFactory.class);
    @Input
    public Property<ArtifactResolver> getResolver() {
        return this.resolver;
    }
    public Property<ArtifactResolver> resolver = getProject().getObjects().property(ArtifactResolver.class);
    @Input
    public Property<ArtifactCollector> getArtifactCollector() {
        return this.artifactCollector;
    }
    public Property<ArtifactCollector> artifactCollector = getProject().getObjects().property(ArtifactCollector.class);
    @Input
    public Property<ArtifactMetadataSource> getArtifactMetadataSource() {
        return this.artifactMetadataSource;
    }
    public Property<ArtifactMetadataSource> artifactMetadataSource = getProject().getObjects().property(ArtifactMetadataSource.class);
    @Input
    public Property<ArtifactHandlerManager> getArtifactHandlerManager() {
        return this.artifactHandlerManager;
    }
    public Property<ArtifactHandlerManager> artifactHandlerManager = getProject().getObjects().property(ArtifactHandlerManager.class);
    @Input
    public Property<DependencyGraphBuilder> getDependencyGraphBuilder() { return this.dependencyGraphBuilder; }
    public Property<DependencyGraphBuilder> dependencyGraphBuilder = getProject().getObjects().property(DependencyGraphBuilder.class);
    @Input
    public Property<ProjectBuilder> getProjectBuilder() { return this.projectBuilder; }
    public Property<ProjectBuilder> projectBuilder = getProject().getObjects().property(ProjectBuilder.class);



    private String[] getIncludeArray() {
        final List<String> includeList = this.getIncludeList().get();
        if (includeList != null && includeList.size() > 0) {
            return this.includes.get().toArray(new String[includeList.size()]);
        }
        return DEFAULT_INCLUDES;
    }

    private String[] getExcludeArray() {
        final List<String> excludeList = this.getExcludeList().get();
        if (excludeList != null && excludeList.size() > 0) {
            return this.includes.get().toArray(new String[excludeList.size()]);
        }
        return DEFAULT_EXCLUDES;
    }

    /**
     *
     * The specification for NAR can be found here:
     * * http://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#nars
     * * http://maven-nar.github.io/
     */
    public NarTask() {
        getArchiveExtension().set(NAR_EXTENSION);
        setMetadataCharset(DefaultManifest.DEFAULT_CONTENT_CHARSET);
        // Add these as separate specs, so they are not affected by the changes to the main spec

        metaInf = (DefaultCopySpec) getRootSpec()
                .addChildBeforeSpec(getMainSpec())
                .into("META-INF");
        
        metaInf.into("classes", spec -> spec.from((Callable<Iterable<File>>) () -> {
            FileCollection classpath = getClasspath();
            return classpath != null ? classpath.filter(File::isDirectory) : Collections.<File>emptyList();
        }));
        
        metaInf.into("lib", spec -> spec.from((Callable<Iterable<File>>) () -> {
            FileCollection classpath = getClasspath();
            return classpath != null ? classpath.filter(File::isFile) : Collections.<File>emptyList();
        }));
        
        metaInf.into("", spec -> {
            spec.from((Callable<File>) NarTask.this::getNifiXml);
            spec.rename(name -> "nifi.xml");
        });

        /**
         * Specify which dependencies are to be bundled in the NAR.
         *
         * The bundled-dependencies contains the jar files that will
         * be used by the processor and accompanying controller services
         * (if the NAR contains a controller service).
         * These jar files will be loaded in the ClassLoader that is dedicated to that processor.
         */
        logger.trace("configure bundled dependencies()");

        metaInf.into(BUNDLE_DIRECTORY,  spec -> spec.from((Callable<Iterable<File>>) () -> {
//            getProject().getConfigurations().runtimeClasspath.get()
//                            .filter { it.name.endsWith("jar") }
//                    .map { it }
            return null;
        }));


//        configureManifest();
//        configureParentNarManifestEntry();
    }
    
    @TaskAction
    public void execute() throws GradleException {
        copyDependencies();

        try {
            generateDocumentation();
        } catch (final Throwable t) {
            
            if (this.getEnforceDocGeneration().get()) {
                // Catch Throwable in case a linkage error such as NoClassDefFoundError occurs
                logger.error("Could not generate extensions' documentation", t);
                throw t;
            } else {
                logger.warn("Could not generate extensions' documentation", t);
            }
        }

        makeNar();
    }
   

    @Internal
    public CopySpec getMetaInf() {
        return metaInf.addChild();
    }

    /**
     * Adds some content to the {@code WEB-INF} directory for this NAR archive.
     *
     * <p>
     *     The given closure is executed to configure a {@link CopySpec}.
     *     The {@code CopySpec} is passed to the closure as its delegate.
     * </p>
     *
     * @param configureClosure The closure to execute
     * @return The newly created {@code CopySpec}.
     */
    public CopySpec nifiInf(Closure configureClosure) {
        return ConfigureUtil.configure(configureClosure, getMetaInf());
    }

    /**
     * Adds some content to the {@code WEB-INF} directory for this NAR archive.
     *
     * <p>The given action is executed to configure a {@link CopySpec}.</p>
     *
     * @param configureAction The action to execute
     * @return The newly created {@code CopySpec}.
     * @since 3.5
     */
    public CopySpec nifiInf(Action<? super CopySpec> configureAction) {
        CopySpec nifiInf = getMetaInf();
        configureAction.execute(nifiInf);
        return nifiInf;
    }

    /**
     * Returns the classpath to include in the NAR archive.
     * Any JAR or ZIP files in this classpath are included in the {@code WEB-INF/lib} directory.
     * Any directories in this classpath are included in the {@code WEB-INF/classes} directory.
     *
     * @return The classpath. Returns an empty collection when there is no classpath to include in the NAR.
     */
    @Nullable
    @Optional
    @Classpath
    public FileCollection getClasspath() {
        return classpath;
    }

    /**
     * Sets the classpath to include in the NAR archive.
     *
     * @param classpath The classpath. Must not be null.
     * @since 4.0
     */
    public void setClasspath(FileCollection classpath) {
        setClasspath((Object) classpath);
    }

    /**
     * Sets the classpath to include in the NAR archive.
     *
     * @param classpath The classpath. Must not be null.
     */
    public void setClasspath(Object classpath) {
        this.classpath = getProject().files(classpath);
    }

    /**
     * Adds files to the classpath to include in the NAR archive.
     *
     * @param classpath The files to add. These are evaluated as per {@link org.gradle.api.Project#files(Object...)}
     */
    public void classpath(Object... classpath) {
        FileCollection oldClasspath = getClasspath();
        this.classpath = getProject().files(oldClasspath != null ? oldClasspath : new ArrayList(), classpath);
    }

    /**
     * Returns the {@code nifi.xml} file to include in the NAR archive.
     * When {@code null}, no {@code nifi.xml} file is included in the NAR.
     *
     * @return The {@code nifi.xml} file.
     */
    @Nullable
    @Optional
    @PathSensitive(PathSensitivity.NONE)
    @InputFile
    public File getNifiXml() {
        return nifiXml;
    }

    /**
     * Sets the {@code nifi.xml} file to include in the NAR archive.
     * When {@code null}, no {@code nifi.xml} file is included in the NAR.
     *
     * @param nifiXml The {@code nifi.xml} file. Maybe null.
     */
    public void setNifiXml(@Nullable File nifiXml) {
        this.nifiXml = nifiXml;
    }


    /**
     * The following functions were lifed from
     *
     * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPluginConvention.java
     *
     * @return
     */

    private File getExtensionsDocumentationFile() {
        final File directory = new File(this.projectBuildDirectory.get().getAsFile(), "META-INF/docs");
        return new File(directory, "extension-manifest.xml");
    }

    private void generateDocumentation() throws GradleException {
        logger.info("Generating documentation for NiFi extensions in the NAR...");

        // Create the ClassLoader for the NAR
        final ExtensionClassLoaderFactory classLoaderFactory = createClassLoaderFactory();

        final ExtensionClassLoader extensionClassLoader;
        try {
            extensionClassLoader = classLoaderFactory.createExtensionClassLoader();
        } catch (final Exception e) {
            if (enforceDocGeneration.get()) {
                throw new GradleException("Failed to create Extension Documentation", e);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to create a ClassLoader for documenting extensions. If this NAR contains any NiFi Extensions, those extensions will not be documented.", e);
                } else {
                    logger.warn("Unable to create a ClassLoader for documenting extensions. If this NAR contains any NiFi Extensions, those extensions will not be documented. " +
                            "Enable mvn DEBUG output for more information (mvn -X).");
                }
                return;
            }
        }


        final File docsFile = getExtensionsDocumentationFile();
        createDirectory(docsFile.getParentFile());

        final File additionalDetailsDir = new File(docsFile.getParentFile(), "additional-details");
        createDirectory(additionalDetailsDir);

        try (final OutputStream out = new FileOutputStream(docsFile)) {

            final XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
            try {
                xmlWriter.writeStartElement("extensionManifest");

                final String nifiApiVersion = extensionClassLoader.getNiFiApiVersion();
                xmlWriter.writeStartElement("systemApiVersion");
                xmlWriter.writeCharacters(nifiApiVersion);
                xmlWriter.writeEndElement();

                xmlWriter.writeStartElement("extensions");

                final Class<?> docWriterClass;
                try {
                    docWriterClass = Class.forName(DOCUMENTATION_WRITER_CLASS_NAME, false, extensionClassLoader);
                } catch (ClassNotFoundException e) {
                    logger.warn("Cannot locate class " + DOCUMENTATION_WRITER_CLASS_NAME + ", so no documentation will be generated for the extensions in this NAR");
                    return;
                }

                logger.debug("Creating Extension Definition Factory for NiFi API version " + nifiApiVersion);

                final ExtensionDefinitionFactory extensionDefinitionFactory = new ExtensionDefinitionFactory(extensionClassLoader);

                final ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(extensionClassLoader);

                    final Set<ExtensionDefinition> processorDefinitions = extensionDefinitionFactory.discoverExtensions(ExtensionType.PROCESSOR);
                    writeDocumentation(processorDefinitions, extensionClassLoader, docWriterClass, xmlWriter, additionalDetailsDir);

                    final Set<ExtensionDefinition> controllerServiceDefinitions = extensionDefinitionFactory.discoverExtensions(ExtensionType.CONTROLLER_SERVICE);
                    writeDocumentation(controllerServiceDefinitions, extensionClassLoader, docWriterClass, xmlWriter, additionalDetailsDir);

                    final Set<ExtensionDefinition> reportingTaskDefinitions = extensionDefinitionFactory.discoverExtensions(ExtensionType.REPORTING_TASK);
                    writeDocumentation(reportingTaskDefinitions, extensionClassLoader, docWriterClass, xmlWriter, additionalDetailsDir);
                } finally {
                    if (currentContextClassLoader != null) {
                        Thread.currentThread().setContextClassLoader(currentContextClassLoader);
                    }
                }

                xmlWriter.writeEndElement();
                xmlWriter.writeEndElement();
            } finally {
                xmlWriter.close();
            }
        } catch (final Exception ioe) {
            throw new GradleException("Failed to create Extension Documentation", ioe);
        }
    }

    private void writeDocumentation(final Set<ExtensionDefinition> extensionDefinitions, final ExtensionClassLoader classLoader,
                                    final Class<?> docWriterClass, final XMLStreamWriter xmlWriter, final File additionalDetailsDir)
            throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        for (final ExtensionDefinition definition : extensionDefinitions) {
            writeDocumentation(definition, classLoader, docWriterClass, xmlWriter);
        }

        final Set<String> extensionNames = extensionDefinitions.stream()
                .map(ExtensionDefinition::getExtensionName)
                .collect(Collectors.toSet());

        try {
            writeAdditionalDetails(classLoader, extensionNames, additionalDetailsDir);
        } catch (final Exception e) {
            throw new IOException("Unable to extract Additional Details", e);
        }

    }

    private void writeDocumentation(final ExtensionDefinition extensionDefinition, final ExtensionClassLoader classLoader,
                                    final Class<?> docWriterClass, final XMLStreamWriter xmlWriter)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, IOException {

        logger.debug("Generating documentation for " + extensionDefinition.getExtensionName() + " using ClassLoader:\n" + classLoader.toTree());
        final Object docWriter = docWriterClass.getConstructor(XMLStreamWriter.class).newInstance(xmlWriter);
        final Class<?> configurableComponentClass = Class.forName("org.apache.nifi.components.ConfigurableComponent", false, classLoader);

        final Class<?> extensionClass = Class.forName(extensionDefinition.getExtensionName(), false, classLoader);
        final Object extensionInstance = extensionClass.newInstance();

        final Method initMethod = docWriterClass.getMethod("initialize", configurableComponentClass);
        initMethod.invoke(docWriter, extensionInstance);

        final Map<String, ServiceAPIDefinition> propertyServiceDefinitions = getRequiredServiceDefinitions(extensionClass, extensionInstance);
        final Set<ServiceAPIDefinition> providedServiceDefinitions = extensionDefinition.getProvidedServiceAPIs();

        if ((providedServiceDefinitions == null || providedServiceDefinitions.isEmpty())
                && (propertyServiceDefinitions == null || propertyServiceDefinitions.isEmpty())) {
            final Method writeMethod = docWriterClass.getMethod("write", configurableComponentClass);
            writeMethod.invoke(docWriter, extensionInstance);
        } else {
            final Class<?> serviceApiClass = Class.forName("org.apache.nifi.documentation.StandardServiceAPI", false, classLoader);
            final List<Object> providedServices = getDocumentationServiceAPIs(serviceApiClass, providedServiceDefinitions);
            final Map<String,Object> propertyServices = getDocumentationServiceAPIs(serviceApiClass, propertyServiceDefinitions);

            final Method writeMethod = docWriterClass.getMethod("write", configurableComponentClass, Collection.class, Map.class);
            writeMethod.invoke(docWriter, extensionInstance, providedServices, propertyServices);
        }
    }

    private List<Object> getDocumentationServiceAPIs(Class<?> serviceApiClass, Set<ServiceAPIDefinition> serviceDefinitions)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Constructor<?> ctr = serviceApiClass.getConstructor(String.class, String.class, String.class, String.class);

        final List<Object> providedServices = new ArrayList<>();

        for (final ServiceAPIDefinition definition : serviceDefinitions) {
            final Object serviceApi = ctr.newInstance(definition.getServiceAPIClassName(), definition.getServiceGroupId(), definition.getServiceArtifactId(), definition.getServiceVersion());
            providedServices.add(serviceApi);
        }
        return providedServices;
    }

    private Map<String,Object> getDocumentationServiceAPIs(Class<?> serviceApiClass, Map<String,ServiceAPIDefinition> serviceDefinitions)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Constructor<?> ctr = serviceApiClass.getConstructor(String.class, String.class, String.class, String.class);

        final Map<String,Object> providedServices = new HashMap<>();

        for (final Map.Entry<String,ServiceAPIDefinition> entry : serviceDefinitions.entrySet()) {
            final String propName = entry.getKey();
            final ServiceAPIDefinition definition = entry.getValue();

            final Object serviceApi = ctr.newInstance(definition.getServiceAPIClassName(), definition.getServiceGroupId(), definition.getServiceArtifactId(), definition.getServiceVersion());
            providedServices.put(propName, serviceApi);
        }
        return providedServices;
    }

    private Map<String,ServiceAPIDefinition> getRequiredServiceDefinitions(final Class<?> extensionClass, final Object extensionInstance)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Map<String,ServiceAPIDefinition> requiredServiceAPIDefinitions = new HashMap<>();

        final Method writeMethod = extensionClass.getMethod("getPropertyDescriptors");
        final List<Object> propertyDescriptors = (List<Object>) writeMethod.invoke(extensionInstance);

        if (propertyDescriptors == null) {
            return requiredServiceAPIDefinitions;
        }

        for (final Object propDescriptor : propertyDescriptors) {
            final Method nameMethod = propDescriptor.getClass().getMethod("getName");
            final String propName = (String) nameMethod.invoke(propDescriptor);

            final Method serviceDefinitionMethod = propDescriptor.getClass().getMethod("getControllerServiceDefinition");
            final Object serviceDefinition = serviceDefinitionMethod.invoke(propDescriptor);

            if (serviceDefinition == null) {
                continue;
            }

            final Class<?> serviceDefinitionClass = (Class<?>) serviceDefinition;
            final ExtensionClassLoader extensionClassLoader = (ExtensionClassLoader) serviceDefinitionClass.getClassLoader();
            final Artifact narArtifact = extensionClassLoader.getNarArtifact();

            final ServiceAPIDefinition serviceAPIDefinition = new StandardServiceAPIDefinition(
                    serviceDefinitionClass.getName(),
                    narArtifact.getGroupId(),
                    narArtifact.getArtifactId(),
                    narArtifact.getBaseVersion()
            );

            requiredServiceAPIDefinitions.put(propName, serviceAPIDefinition);
        }

        return requiredServiceAPIDefinitions;
    }

    private void writeAdditionalDetails(final ExtensionClassLoader classLoader, final Set<String> extensionNames, final File additionalDetailsDir)
            throws URISyntaxException, IOException, GradleException {

        for (final URL url : classLoader.getURLs()) {
            final File file = new File(url.toURI());
            final String filename = file.getName();
            if (!filename.endsWith(".jar")) {
                continue;
            }

            writeAdditionalDetails(file, extensionNames, additionalDetailsDir);
        }
    }

    private void writeAdditionalDetails(final File file, final Set<String> extensionNames, final File additionalDetailsDir) throws IOException, GradleException {
        final JarFile jarFile = new JarFile(file);

        for (final Enumeration<JarEntry> jarEnumeration = jarFile.entries(); jarEnumeration.hasMoreElements();) {
            final JarEntry jarEntry = jarEnumeration.nextElement();

            final String entryName = jarEntry.getName();
            if (!entryName.startsWith("docs/")) {
                continue;
            }

            final int nextSlashIndex = entryName.indexOf("/", 5);
            if (nextSlashIndex < 0) {
                continue;
            }

            final String componentName = entryName.substring(5, nextSlashIndex);
            if (!extensionNames.contains(componentName)) {
                continue;
            }

            if (jarEntry.isDirectory()) {
                continue;
            }

            if (entryName.length() < nextSlashIndex + 1) {
                continue;
            }

            logger.debug("Found file " + entryName + " in " + file + " that consists of documentation for " + componentName);
            final File componentDirectory = new File(additionalDetailsDir, componentName);
            final String remainingPath = entryName.substring(nextSlashIndex + 1);
            final File destinationFile = new File(componentDirectory, remainingPath);

            createDirectory(destinationFile.getParentFile());

            try (final InputStream in = jarFile.getInputStream(jarEntry);
                 final OutputStream out = new FileOutputStream(destinationFile)) {
                copy(in, out);
            }
        }
    }

    private void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
    }


    private ExtensionClassLoaderFactory createClassLoaderFactory() {
        return new ExtensionClassLoaderFactory.Builder()
                .artifactResolver(this.getResolver().get())
                .dependencyGraphBuilder(this.getDependencyGraphBuilder().get())
                // TODO convert to maven repository container objects.
                //.localRepository(this.getRepositories().get())
                .log(getLog())
                // TODO Wants maven project
                //.project(getProject())
                .projectBuilder(this.getProjectBuilder().get())
                .artifactHandlerManager(this.getArtifactHandlerManager().get())
                .build();
    }


    private void createDirectory(final File file) throws GradleException {
        if (!file.exists()) {
            try {
                Files.createDirectories(file.toPath());
            } catch (IOException e) {
                throw new GradleException("Could not create directory " + file, e);
            }
        }
    }


    private void copyDependencies() throws GradleException {
        DependencyStatusSets dss = getDependencySets(this.failOnMissingClassifierArtifact.get());
        Set artifacts = dss.getResolvedDependencies();

        for (Object artifactObj : artifacts) {
            copyArtifact((Artifact) artifactObj);
        }

        artifacts = dss.getSkippedDependencies();
        for (Object artifactOjb : artifacts) {
            Artifact artifact = (Artifact) artifactOjb;
            logger.debug(artifact.getFile().getName() + " already exists in destination.");
        }
    }

    protected void copyArtifact(Artifact artifact) throws GradleException {
        String destFileName = DependencyUtil.getFormattedFileName(artifact, false);
        final File destDir = DependencyUtil.getFormattedOutputDirectory(false, false, false, false, false, getDependenciesDirectory(), artifact);
        final File destFile = new File(destDir, destFileName);
        copyFile(artifact.getFile(), destFile);
    }

    protected Artifact getResolvedPomArtifact(Artifact artifact) {
        Artifact pomArtifact = this.getFactory().get()
                .createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "", "pom");
        // Resolve the pom artifact using repos
        try {
            this.getRepositories().get();
            // FIXME: I do not have the maven repositories.
            // this.getResolver().get().resolve(pomArtifact, this.getRepositories().get(), this.localRepo);
            this.getResolver().get().resolve(pomArtifact, null, null);
        } catch (ArtifactResolutionException | ArtifactNotFoundException e) {
            logger.info(e.getMessage());
        }
        return pomArtifact;
    }

    protected ArtifactsFilter getMarkedArtifactFilter() {
        return new DestFileFilter(
                this.getOverWriteReleases().get(),
                this.getOverWriteSnapshots().get(),
                this.getOverWriteIfNewer().get(),
                false, false, false, false, false, getDependenciesDirectory());
    }


    protected DependencyStatusSets getDependencySets(boolean stopOnFailure) throws GradleException {
        // add filters in well known order, least specific to most specific
        FilterArtifacts filter = new FilterArtifacts();

        filter.addFilter(new ProjectTransitivityFilter(getProject().getDependencies(), false));
        filter.addFilter(new ScopeFilter(this.getIncludeScope().get(), this.getExcludeScope().get()));
        filter.addFilter(new TypeFilter(this.getIncludeTypes().get(), this.getExcludeTypes().get()));
        filter.addFilter(new ClassifierFilter(this.getIncludeClassifiers().get(), this.getExcludeClassifiers().get()));
        filter.addFilter(new GroupIdFilter(this.getIncludeGroupIds().get(), this.getExcludeGroupIds().get()));
        filter.addFilter(new ArtifactIdFilter(this.getIncludeArtifactIds().get(), this.getExcludeArtifactIds().get()));

        // explicitly filter our nar dependencies
        filter.addFilter(new TypeFilter("", "nar"));

        // start with all artifacts.
        ArtifactHandler artifactHandler = getProject().getArtifacts();
        // TODO How to get a set of artifacts from gradle?
        Set<Artifact> artifacts = null; //  artifactHandler;

        // perform filtering
        try {
            artifacts = filter.filter(artifacts);
        } catch (ArtifactFilterException e) {
            throw new GradleException(e.getMessage(), e);
        }

        // transform artifacts if classifier is set
        final DependencyStatusSets status;
        if (StringUtils.isNotEmpty(this.copyDepClassifier.get())) {
            status = getClassifierTranslatedDependencies(artifacts, stopOnFailure);
        } else {
            status = filterMarkedDependencies(artifacts);
        }

        return status;
    }

    protected DependencyStatusSets getClassifierTranslatedDependencies(Set artifacts, boolean stopOnFailure) throws  GradleException
    {
        Set unResolvedArtifacts = new HashSet();
        Set resolvedArtifacts = artifacts;
        DependencyStatusSets status = new DependencyStatusSets();

        // possibly translate artifacts into a new set of artifacts based on the
        // classifier and type
        // if this did something, we need to resolve the new artifacts
        if (StringUtils.isNotEmpty(this.getCopyDepClassifier().get())) {
            ArtifactTranslator translator = new ClassifierTypeTranslator(
                    this.getCopyDepClassifier().get(),
                    this.getType().get(),
                    this.getFactory().get());
            artifacts = translator.translate(artifacts, getLog());

            status = filterMarkedDependencies(artifacts);

            // the unskipped artifacts are in the resolved set.
            artifacts = status.getResolvedDependencies();

            // resolve the rest of the artifacts
            // FIXME: the repositories are maven local and remote.
            ArtifactsResolver artifactsResolver = new DefaultArtifactsResolver(this.getResolver().get(),
                    null, null, stopOnFailure);
            // FIXME: the logger is wrong.
            resolvedArtifacts = null; // artifactsResolver.resolve(artifacts, null);

            // calculate the artifacts not resolved.
            unResolvedArtifacts.addAll(artifacts);
            unResolvedArtifacts.removeAll(resolvedArtifacts);
        }

        // return a bean of all 3 sets.
        status.setResolvedDependencies(resolvedArtifacts);
        status.setUnResolvedDependencies(unResolvedArtifacts);

        return status;
    }

    // Construct a maven logger from the actual logger.
    private Log getLog()
    {
        if (logger == null) {
            return null; // this.log = new SystemStreamLog();
        }

        return null;
    }

    protected DependencyStatusSets filterMarkedDependencies(Set artifacts) throws GradleException {
        // remove files that have markers already
        FilterArtifacts filter = new FilterArtifacts();
        filter.clearFilters();
        filter.addFilter(getMarkedArtifactFilter());

        Set unMarkedArtifacts;
        try {
            unMarkedArtifacts = filter.filter(artifacts);
        } catch (ArtifactFilterException e) {
            throw new GradleException(e.getMessage(), e);
        }

        // calculate the skipped artifacts
        Set skippedArtifacts = new HashSet();
        skippedArtifacts.addAll(artifacts);
        skippedArtifacts.removeAll(unMarkedArtifacts);

        return new DependencyStatusSets(unMarkedArtifacts, null, skippedArtifacts);
    }

    protected void copyFile(File artifact, File destFile) throws GradleException {
        try {
            logger.info("Copying " + (this.getOutputAbsoluteArtifactFilename().get() ? artifact.getAbsolutePath() : artifact.getName()) + " to " + destFile);
            FileUtils.copyFile(artifact, destFile);
        } catch (Exception e) {
            throw new GradleException("Error copying artifact from " + artifact + " to " + destFile, e);
        }
    }

    private File getClassesDirectory() {
        final File outputDirectory = this.projectBuildDirectory.get().getAsFile();
        return new File(outputDirectory, "classes");
    }

    private File getDependenciesDirectory() {
        return new File(getClassesDirectory(), "META-INF/bundled-dependencies");
    }

    private void makeNar() throws GradleException {
        File narFile = createArchive();

        // TODO What is the propper way to write the nar?
        if (this.classifier.get() != null) {
            //this.getProjectHelper().get().attachArtifact(getProject(), "nar", this.classifier.get(), narFile);
        } else {
            //getProject().getArtifacts().setFile(narFile);
        }
    }

    public File createArchive() throws GradleException {
        final File outputDirectory = this.projectBuildDirectory.get().getAsFile();
        File narFile = getNarFile(outputDirectory, this.finalName.get(), this.classifier.get());
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(this.jarArchiver.get());
        archiver.setOutputFile(narFile);
        archiver.setForced(this.forceCreation.get());

        try {
            File contentDirectory = getClassesDirectory();
            if (contentDirectory.exists()) {
                archiver.getArchiver().addDirectory(contentDirectory, getIncludeArray(), getExcludeArray());
            } else {
                logger.warn("NAR will be empty - no content was marked for inclusion!");
            }

            File extensionDocsFile = getExtensionsDocumentationFile();
            if (extensionDocsFile.exists()) {
                archiver.getArchiver().addFile(extensionDocsFile, "META-INF/docs/" + extensionDocsFile.getName());
            } else {
                logger.warn("NAR will not contain any Extensions' documentation - no META-INF/" + extensionDocsFile.getName() + " file found!");
            }

            File additionalDetailsDirectory = new File(getExtensionsDocumentationFile().getParentFile(), "additional-details");
            if (additionalDetailsDirectory.exists()) {
                archiver.getArchiver().addDirectory(additionalDetailsDirectory, "META-INF/docs/additional-details/");
            }

            final MavenArchiveConfiguration archive = this.getArchive().get();

            File existingManifest = this.getDefaultManifestFiles().get().getAsFile();
            if (this.getUseDefaultManifestFile().get() && existingManifest.exists() && archive.getManifestFile() == null) {
                logger.info("Adding existing MANIFEST to archive. Found under: " + existingManifest.getPath());
                archive.setManifestFile(existingManifest);
            }

            // automatically add the artifact id, group id, and version to the manifest
            archive.addManifestEntry("Nar-Id", this.narId.get());
            archive.addManifestEntry("Nar-Group", this.narGroup.get());
            archive.addManifestEntry("Nar-Version", this.narVersion.get());

            // look for a nar dependency
            NarDependency narDependency = getNarDependency();
            if (narDependency != null) {
                final String narDependencyGroup = notEmpty(this.narDependencyGroup.get()) ? this.narDependencyGroup.get() : narDependency.getGroupId();
                final String narDependencyId = notEmpty(this.narDependencyId.get()) ? this.narDependencyId.get() : narDependency.getArtifactId();
                final String narDependencyVersion = notEmpty(this.narDependencyVersion.get()) ? this.narDependencyVersion.get() : narDependency.getVersion();

                archive.addManifestEntry("Nar-Dependency-Group", narDependencyGroup);
                archive.addManifestEntry("Nar-Dependency-Id", narDependencyId);
                archive.addManifestEntry("Nar-Dependency-Version", narDependencyVersion);
            }

            // add build information when available

            if (notEmpty(this.buildTag.get())) {
                archive.addManifestEntry("Build-Tag", this.buildTag.get());
            }
            if (notEmpty(this.getBuildBranch().get())) {
                archive.addManifestEntry("Build-Branch", this.buildBranch.get());
            }
            if (notEmpty(this.getBuildRevision().get())) {
                archive.addManifestEntry("Build-Revision", this.buildRevision.get());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat(BUILD_TIMESTAMP_FORMAT);
            archive.addManifestEntry("Build-Timestamp", dateFormat.format(new Date()));

            archive.addManifestEntry("Clone-During-Instance-Class-Loading", String.valueOf(this.cloneDuringInstanceClassLoading.get()));

            archiver.createArchive(getProject(), archive);
            return narFile;
        } catch (ArchiverException | GradleException ex) {
            throw new GradleException("Error assembling NAR", ex);
        }
    }

    private boolean notEmpty(String value) {
        return value != null && !value.isEmpty();
    }


    protected File getNarFile(File basedir, String finalName, String classifier) {
        if (classifier == null) {
            classifier = "";
        } else if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }

        return new File(basedir, finalName + classifier + ".nar");
    }

    private NarDependency getNarDependency() throws GradleException {
        NarDependency narDependency = null;

        // get nar dependencies
        FilterArtifacts filter = new FilterArtifacts();
        filter.addFilter(new TypeFilter("nar", ""));

        // start with all artifacts.
        ArtifactHandler artifactHandler = getProject().getArtifacts();
        // FIXME: how do I get a set of the artifacts? dependencies?
        Set<Artifact> artifacts = null; // artifactHandler;

        // perform filtering
        try {
            artifacts = filter.filter(artifacts);
        } catch (ArtifactFilterException e) {
            throw new GradleException(e.getMessage(), e);
        }

        // ensure there is a single nar dependency
        if (artifacts.size() > 1) {
            throw new GradleException("Each NAR represents a ClassLoader. A NAR dependency allows that NAR's ClassLoader to be "
                    + "used as the parent of this NAR's ClassLoader. As a result, only a single NAR dependency is allowed.");
        } else if (artifacts.size() == 1) {
            final Artifact artifact = (Artifact) artifacts.iterator().next();

            narDependency = new NarDependency(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion());
        }

        return narDependency;
    }

    private static class NarDependency {

        final String groupId;
        final String artifactId;
        final String version;

        public NarDependency(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }
    }

}


