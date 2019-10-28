package org.babeloff.gradle.api.tasks


import org.apache.log4j.LogManager
import org.apache.nifi.nar.NarManifestEntry
import org.babeloff.gradle.api.annotations.Parameter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar
import org.gradle.work.Incremental
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.jar.JarFile
import javax.inject.Inject
import javax.xml.stream.XMLOutputFactory

/**
 * Assembles a NIFI archive.
 *
 * The specification for which can be found at:
 * https://gitbox.apache.org/repos/asf?p=nifi-maven.git
 * https://gitbox.apache.org/repos/asf?p=nifi-maven.git;a=blob;f=src/main/java/org/apache/nifi/NifiMojo.java
 *
 * The following is *not* the same http://maven-nifi.github.io/index.html
 *
 * @author Fred Eisele
 */
abstract class NifiTask : Jar()
{
    companion object {
        private val logger = LogManager.getLogger(NifiTask::class.java)

        val NIFI_EXTENSION = "nar"

        val DEPENDENCIES_FILE = "META-INF/DEPENDENCIES"
        val LICENSE_FILE = "META-INF/LICENSE"
        val NOTICE_FILE = "META-INF/NOTICE"

        val SERVICES_DIRECTORY = "META-INF/services/"
        val BUNDLE_DIRECTORY = "META-INF/bundled-dependencies/"
        val DOCUMENTATION_WRITER_CLASS_NAME = "org.apache.nifi.documentation.xml.XmlDocumentationWriter"

        val DEFAULT_INCLUDES = mutableListOf("**/**")
        val DEFAULT_EXCLUDES = mutableListOf("**/package.html")

        val BUILD_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        val DATA_FORMAT = SimpleDateFormat(BUILD_TIMESTAMP_FORMAT)
        val VERSION_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    }

    init {
        logger.debug("initializing nifi task")
    }

    /**
     * Injected Services
     */
    @get:Inject
    abstract val objectFactory: ObjectFactory

    @get:Inject
    abstract val layout: ProjectLayout

    @get:Inject
    abstract val fileSystemOps: FileSystemOperations


    /**
     * POM
     */
//    @Parameter(defaultValue = "\${project}", readonly = true, required = true)
//    abstract val project: Project

//    @Parameter(defaultValue = "${session}", readonly = true, required = true)
//    public MavenSession session;

    @Parameter(property = "inputDir",
            defaultValue = "\${project.build.lib", readonly = true, required = true)
    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    /**
     * List of files to include. Specified as fileset patterns.
     */
    @Parameter(property = "includes")
    @get:Input
    abstract val includeList: ListProperty<String>

    /**
     * List of files to exclude. Specified as fileset patterns.
     */
    @Parameter(property = "excludes")
    @get:Input
    abstract val excludeList: ListProperty<String>

    /**
     * Name of the generated NIFI.
     *
     */
    @Parameter(alias = "nifiName", property = "nifi.finalName",
            defaultValue = "\${project.getName()}", required = true)
    @get:Input
    abstract val finalName: Property<String>

    /**
     * The Jar archiver.
     *
     * \@\component role="org.codehaus.plexus.archiver.Archiver" roleHint="jar"
     */
//    @Component(role = org.gradle.internal.impldep.org.codehaus.plexus.archiver.Archiver::class, hint = "jar")
//    protected var jarArchiver: JarArchiver

    /**
     * The archive configuration to use.
     *
     * See [the documentation for Maven Archiver](http://maven.apache.org/shared/maven-archiver/index.html).
     *
     */
//    @Parameter(property = "archive")
//    abstract val archive: MavenArchiveConfiguration = MavenArchiveConfiguration()

    /**
     * Path to the default MANIFEST file to use. It will be used if
     * `useDefaultManifestFile` is set to `true`.
     *
     */
    @Parameter(property = "defaultManifestFile",
            defaultValue = "\${project.build.outputDirectory}/META-INF/MANIFEST.MF", readonly = true, required = true)
    @get:OutputFile
    abstract val defaultManifestFiles: RegularFileProperty

    /**
     * Set this to `true` to enable the use of the
     * `defaultManifestFile`.
     *
     * @since 2.2
     */
    @Parameter(property = "nifi.useDefaultManifestFile", defaultValue = "false")
    @get:Input
    abstract val useDefaultManifestFile: Property<Boolean>

//    @Component
//    abstract val  projectHelper: MavenProjectHelper = MavenProjectHelperDefault()

    /**
     * Whether creating the archive should be forced.
     *
     */
    @Parameter(property = "nifi.forceCreation", defaultValue = "false")
    @get:Input
    abstract val forceCreation: Property<Boolean>

    /**
     * Classifier to add to the artifact generated.
     * If given, the artifact will be an attachment instead.
     */
//    @Parameter(property = "classifier")
//    @get:Input
//    abstract val archiveClassifier: Property<String>
//

//    @Component
//    abstract val  installer: ArtifactInstaller =  ArtifactInstallerDefault()


//    @Component
//    abstract val  repositoryFactory: ArtifactRepositoryFactory = ArtifactRepositoryFactoryDefault()

    /**
     * This only applies if the classifier parameter is used.
     *
     */
    @Parameter(property = "mdep.failOnMissingClassifierArtifact", defaultValue = "true", required = false)
    @get:Input
    abstract val failOnMissingClassifierArtifact: Property<Boolean>

    /**
     * Comma Separated list of Types to include. Empty String indicates include
     * everything (default).
     *
     */
    @Parameter(property = "includeTypes", required = false)
    @get:Input
    abstract val includeTypes: Property<String>

    /**
     * Comma Separated list of Types to exclude. Empty String indicates don't
     * exclude anything (default).
     *
     */
    @Parameter(property = "excludeTypes", required = false)
    @get:Input
    abstract val excludeTypes: Property<String>

    /**
     * Scope to include. An Empty string indicates all scopes (default).
     *
     */
    @Parameter(property = "includeScope", required = false)
    @get:Input
    abstract val includeScope: Property<String>

    /**
     * Scope to exclude. An Empty string indicates no scopes (default).
     *
     */
    @Parameter(property = "excludeScope", required = false)
    @get:Input
    abstract val excludeScope: Property<String>

    /**
     * Comma Separated list of Classifiers to include. Empty String indicates
     * include everything (default).
     *
     */
    @Parameter(property = "includeClassifiers", required = false)
    @get:Input
    abstract val includeClassifiers: Property<String>

    /**
     * Comma Separated list of Classifiers to exclude. Empty String indicates
     * don't exclude anything (default).
     *
     */
    @Parameter(property = "excludeClassifiers", required = false)
    @get:Input
    abstract val excludeClassifiers: Property<String>

    /**
     * Specify classifier to look for. Example: sources
     *
     */
    @Parameter(property = "classifier", required = false)
    @get:Input
    abstract val copyDepClassifier: Property<String>

    /**
     * Specify type to look for when constructing artifact based on classifier.
     * Example: java-source,jar,war, nifi
     *
     */
    @Parameter(property = "type", required = true, defaultValue = "nifi")
    @get:Input
    abstract val type: Property<String>


    /**
     * Comma separated list of Artifact names to include.
     *
     */
    @Parameter(property = "includeArtifacts", required = false)
    @get:Input
    abstract val includeArtifactIds: Property<String>

     /**
 * Comma separated list of Artifact names too exclude.
 *
 */
@Parameter(property = "excludeArtifacts", required = false)
@get:Input
abstract val excludeArtifactIds: Property<String>

    /**
     * Comma separated list of GroupIds to include.
     *
     */
    @Parameter(property = "includeGroupIds", required = false)
    @get:Input
    abstract val includeGroupIds: Property<String>

    /**
     * Comma separated list of GroupId Names to exclude.
     *
     */
    @Parameter(property = "excludeArtifacts", required = false)
    @get:Input
    abstract val excludeGroupIds: Property<String>

    /**
     * Directory to store flag files
     *
     */
    @Parameter(property = "markersDirectory", required = true,
            defaultValue = "\${project.build.directory}/dependency-maven-plugin-markers")
    @get:OutputDirectory
    abstract val markersDirectory: DirectoryProperty

    /**
     * Overwrite release artifacts
     *
     */
    @Parameter(property = "overWriteReleases", required = true)
    @get:Input
    abstract val overWriteReleases: Property<Boolean>

    /**
     * Overwrite snapshot artifacts
     *
     */
    @Parameter(property = "overWriteSnapshots", required = true)
    @get:Input
    abstract val overWriteSnapshots: Property<Boolean>

    /**
     * Overwrite artifacts that don't exist or are older than the source.
     *
     */
    @Parameter(property = "overWriteIfNewer", required = true, defaultValue = "true")
    @get:Input
    abstract val overWriteIfNewer: Property<Boolean>

    @Parameter(property = "projectBuildDirectory", required = true, defaultValue = "\${project.build.directory}")
    @get:OutputDirectory
    abstract val projectBuildDirectory: DirectoryProperty

    /**
     * Used to look up Artifacts in the remote repository.
     */
//    @Component
//    abstract val  factory: ArtifactFactory = ArtifactFactoryDefault()

    /**
     * Used to look up Artifacts in the remote repository.
     *
     */
//    @Component
//    abstract val  resolver: ArtifactResolver = ArtifactResolverDefault()

    /**
     * Container of Repositories used by the resolver
     */
//    @Parameter(property = "project.remoteArtifactRepositories", required = true, readonly = true)
//    @get:Input
//    abstract val repositories: Property<RepositoryHandler>
//
//    init {
//        logger.debug("initializing nifi task repositories")
//        //repositories.set(project.repositories)
//    }

    /**
     * To look up Archiver/UnArchiver implementations
     *
     */
//    @Component
//    abstract val archiverManager: ArchiverManager = ArchiverManagerDefault()

    /**
     * Contains the full list of projects in the reactor.
     *
     */
    @Parameter(property = "reactorProjects", required = true, readonly = true)
    @get:Input
    abstract val reactorProjects: Property<String>

    /**
     * If the plugin should be silent.
     *
     */
    @Parameter(property = "silent", required = false, defaultValue = "false")
    @get:Input
    abstract val silent: Property<Boolean>

    /**
     * The dependency tree builder to use for verbose output.
     */
//    @Component
//    abstract val  dependencyGraphBuilder: DependencyGraphBuilder = DependencyGraphBuilder(
//            null, // DependencyToComponentIdResolver componentIdResolver,
//            null, // ComponentMetaDataResolver componentMetaDataResolver,
//            null, // ResolveContextToComponentResolver resolveContextToComponentResolver,
//            null, // ModuleConflictHandler moduleConflictHandler,
//            null, // CapabilitiesConflictHandler capabilitiesConflictHandler,
//            null, // Spec<? super DependencyMetadata> edgeFilter,
//            null, // AttributesSchemaInternal attributesSchema,
//            null, // ModuleExclusions moduleExclusions,
//            null, // BuildOperationExecutor buildOperationExecutor,
//            null, // ModuleReplacementsData moduleReplacementsData,
//            null, // DependencySubstitutionApplicator dependencySubstitutionApplicator,
//            null, // ComponentSelectorConverter componentSelectorConverter,
//            null, // ImmutableAttributesFactory attributesFactory,
//            null, // VersionSelectorScheme versionSelectorScheme,
//            null, // Comparator<Version> versionComparator,
//            null // VersionParser versionParser
//    )

    /**
     * *
     * The [ArtifactHandlerManager] into which any extension [ArtifactHandler]
     * instances should have been injected when the extensions were loaded.
     */
//    @Component
//    abstract val  artifactHandlerManager: ArtifactHandlerManager = ArtifactHandlerManagerDefault()

    /**
     * Output absolute filename for resolved artifacts
     *
     */
    @Parameter(property = "outputAbsoluteArtifactFilename", defaultValue = "false", required = false)
    @get:Input
    abstract val outputAbsoluteArtifactFilename: Property<Boolean>

    /**
     * The values to use for populating the Nifi-Group, Nifi-Id, and Nifi-Version in the MANIFEST file.
     * By default these values will be set to the standard Maven project equivalents,
     * but they may be overridden through properties.
     *
     * For example if the build.gradle.kts for the nifi-test-nifi contained the following:
     *
     * groupId = "org.apache.nifi"
     * artifactId = nifi-test-nifi"
     * version = "1.0"
     *
     * nifi {
     * nifiGroup = "org.apache.nifi.overridden"
     * nifiId = "nifi-overridden-test-nifi"
     * nifiVersion = "2.0"
     * }
     *
     * It would produce a MANIFEST with:
     *
     * Nifi-Id: nifi-overridden-test-nifi
     * Nifi-Group: org.apache.nifi.overridden
     * Nifi-Version: 2.0
     *
     */

    @Parameter(property = "nifiGroup", defaultValue = "\${project.groupId}", required = true)
    @get:Input
    abstract val nifiGroup: Property<String>

    @Parameter(property = "nifiId", defaultValue = "\${project.artifactId}", required = true)
    @get:Input
    abstract val nifiId: Property<String>

    @Parameter(property = "nifiVersion", defaultValue = "\${project.version}", required = true)
    @get:Input
    abstract val nifiVersion: Property<String>


    @Parameter(property = "nifiDependency", required = false, defaultValue = "false")
    @get:Input
    abstract val nifiDependency: Property<Boolean>

    @Parameter(property = "nifiDependencyGroup", required = false, defaultValue = "null")
    @get:Input
    abstract val nifiDependencyGroup: Property<String?>

    @Parameter(property = "nifiDependencyId", required = false, defaultValue = "null")
    @get:Input
    abstract val nifiDependencyId: Property<String?>

    @Parameter(property = "nifiDependencyVersion", required = false, defaultValue = "null")
    @get:Input
    abstract val nifiDependencyVersion: Property<String?>


    /**
     * Build info to be populated in MANIFEST.
     */

    @Parameter(property = "buildTag", defaultValue = "\${project.scm.tag}", required = false)
    @get:Input
    abstract val buildTag: Property<String>

    @Parameter(property = "buildBranch", defaultValue = "\${buildBranch}", required = false)
    @get:Input
    abstract val buildBranch: Property<String>

    @Parameter(property = "buildRevision", defaultValue = "\${buildRevision}", required = false)
    @get:Input
    abstract val buildRevision: Property<String>


    /**
     * Allows a NIFI to specify if it's resources should be cloned when a component
     * that depends on this NIFI is performing class loader isolation.
     */
    @Parameter(property = "cloneDuringInstanceClassLoading", defaultValue = "false", required = false)
    @get:Input
    abstract val cloneDuringInstanceClassLoading: Property<Boolean>

    @Parameter(property = "enforceDocGeneration", defaultValue = "false", required = false)
    @get:Input
    abstract val enforceDocGeneration: Property<Boolean>

    /**
     * The [RepositorySystemSession] used for obtaining the local and remote artifact repositories.
     */
    //@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    //private RepositorySystemSession getRepoSession();


    /**
     * The [ProjectBuilder] used to generate the [MavenProject]
     * for the nifi artifact the dependency tree is being generated for.
     */
//    @Component
//    abstract val  projectBuilder: ProjectBuilder = ProjectBuilderDefault()


    /**
     * The following functions were lifted from
     *
     * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPluginConvention.java
     *
     * @return
     */

    private fun extensionsDocumentationFile(): File {
        val directory = File(this.projectBuildDirectory.get().asFile, "META-INF/docs")
        return File(directory, "extension-manifest.xml")
    }

    private fun classesDirectory(): File {
        val outputDirectory = this.projectBuildDirectory.get().asFile
        return File(outputDirectory, "classes")
    }

    private fun dependenciesDirectory(): File {
        return File(classesDirectory(), "META-INF/bundled-dependencies")
    }

//    private
//    // get nifi dependencies
//    // start with all artifacts.
//    // FIXME: how do I get a set of the artifacts? dependencies?
//    // artifactHandler;
//    // perform filtering
//    // ensure there is a single nifi dependency
//    abstract val nifiDependency: NifiDependency?
//        @Throws(GradleException::class)
//        get() {
//            abstract val nifiDependency: NifiDependency? = null
////            abstract val  filter = FilterArtifacts()
////            filter.addFilter(TypeFilter("nifi", ""))
//            abstract val artifactHandler = project.artifacts
////            abstract val artifacts: Set<Artifact>? = artifactHandler.
//////            try {
//////                // TODO: artifacts = filter.filter(artifacts).orEmpty()
////////            } catch (ex: ArtifactFilterException) {
////////                throw GradleException(ex.message!!, ex)
//////            }
////
////            if (artifacts.size > 1) {
////                throw GradleException("Each NIFI represents a ClassLoader. A NIFI dependency allows that NIFI's ClassLoader to be " + "used as the parent of this NIFI's ClassLoader. As a result, only a single NIFI dependency is allowed.")
////            }
////            if (artifacts.size == 1) {
////                abstract val  artifact = artifacts.iterator().next() as Artifact
////                nifiDependency = NifiDependency(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion())
////            }
//
//            return nifiDependency
//        }

    /**
     * Configuration phase
     */

    @get:Internal
    abstract val bundledDependencies : ListProperty<Any>
    init {
       logger.info("configuring nifi task")
        bundledDependencies.set(listOf())
    }

    /** Configure Bundled Dependencies */
    init {
        into(BUNDLE_DIRECTORY) {
            from( bundledDependencies.get() )
        }
    }

    @get:Internal
    abstract val parentArchiveConfig : Property<Configuration?>
    init {
        logger.info("configuring nifi task")
        parentArchiveConfig.set(null)
    }

    /** Configure Manifest */
    init {
        val attr = this.manifest.attributes
        attr.putIfAbsent(NarManifestEntry.NAR_GROUP.manifestName, this.nifiGroup)
        attr.putIfAbsent(NarManifestEntry.NAR_ID.manifestName, this.nifiId)
        attr.putIfAbsent(NarManifestEntry.NAR_VERSION.manifestName, this.nifiVersion)
    }

    /** Configure Parent Manifest */
    init {
//        val config = this.parentArchiveConfig.get()
//        when {
//            config == null -> {}
//            config.all.size > 1 ->
//                throw RuntimeException("Only one parent nar dependency allowed in nar configuration but found ${config.all.size} configurations")
//            config.all.isEmpty() -> {}
//            else -> {
//                val attr = this.manifest.attributes
//                attr.putIfAbsent(NarManifestEntry.NAR_DEPENDENCY_GROUP.manifestName, this.nifiDependencyGroup)
//                attr.putIfAbsent(NarManifestEntry.NAR_DEPENDENCY_ID.manifestName, this.nifiDependencyId)
//                attr.putIfAbsent(NarManifestEntry.NAR_DEPENDENCY_VERSION.manifestName, this.nifiDependencyVersion)
//            }
//        }
    }

    /**
     *
     * The specification for NIFI can be found here:
     * * http://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#nifis
     * * http://maven-nifi.github.io/
     *
     */
    @TaskAction
    @Throws(GradleException::class)
    fun execute() {

        /**
         * Specify which dependencies are to be bundled in the NIFI.
         *
         * The bundled-dependencies contains the jar files that will
         * be used by the processor and accompanying controller services
         * (if the NIFI contains a controller service).
         * These jar files will be loaded in the ClassLoader that is dedicated to that processor.
         */
        logger.trace("execute bundled dependencies()")

        // TODO
//        getMetaInf().into(BUNDLE_DIRECTORY, { spec ->
//            spec.from({ null }//            getProject().getConfigurations().runtimeClasspath.get()
//                    //                            .filter { it.name.endsWith("jar") }
//                    //                    .map { it }
//                    as Callable<Iterable<File>>)
//        })


        //        configureManifest();
        //        configureParentNifiManifestEntry();

//        copyDependencies()
//
//        try {
//            generateDocumentation()
//        } catch (t: Throwable) {
//
//            if (this.enforceDocGeneration.get()) {
//                // Catch Throwable in case a linkage error such as NoClassDefFoundError occurs
//                logger.error("Could not generate extensions' documentation", t)
//                throw t
//            } else {
//                logger.warn("Could not generate extensions' documentation", t)
//            }
//        }
//
//        val nifiFile = createArchive(archive)
//
//        // TODO What is the proper way to write the nifi?
//        if (archiveClassifier != null) {
//            //this.getProjectHelper().attachArtifact(getProject(), "nifi", this.getArchiveClassifier(), nifiFile);
//        } else {
//            //getProject().getArtifacts().setFile(nifiFile);
//        }
//
//        archive
    }


    /**
     * Adds some content to the `WEB-INF` directory for this NIFI archive.
     *
     *
     *
     * The given closure is executed to configure a [CopySpec].
     * The `CopySpec` is passed to the closure as its delegate.
     *
     *
     * @param configureClosure The closure to execute
     * @return The newly created `CopySpec`.
     */
//    fun nifiInf(configureClosure: Closure<*>): CopySpec {
//        return ConfigureUtil.configure(configureClosure, getMetaInf())
//    }

    /**
     * Adds some content to the `WEB-INF` directory for this NIFI archive.
     *
     *
     * The given action is executed to configure a [CopySpec].
     *
     * @param configureAction The action to execute
     * @return The newly created `CopySpec`.
     * @since 3.5
     */
//    fun nifiInf(configureAction: Action<in CopySpec>): CopySpec {
//        abstract val  nifiInf = getMetaInf()
//        configureAction.execute(nifiInf)
//        return nifiInf
//    }


//
//    /**
//     * Adds files to the classpath to include in the NIFI archive.
//     *
//     * @param classpath The files to add. These are evaluated as per [org.gradle.api.Project.files]
//     */
//    fun classpath(vararg classpath: Any) {
//        abstract val  oldClasspath = getClasspath()
//        this.classpath = getProject().files(if (oldClasspath != null) oldClasspath else ArrayList(), classpath)
//    }

//    @Throws(GradleException::class)
//    private fun generateDocumentation() {
//        logger.debug("Generating documentation for NiFi extensions in the NIFI...")

        // Create the ClassLoader for the NIFI
//        abstract val  classLoaderFactory = createClassLoaderFactory()

//        abstract val  extensionClassLoader: ExtensionClassLoader
//        try {
//            extensionClassLoader = classLoaderFactory.createExtensionClassLoader()
//        } catch (e: Exception) {
//            if (this.enforceDocGeneration!!) {
//                throw GradleException("Failed to create Extension Documentation", e)
//            } else {
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Unable to create a ClassLoader for documenting extensions. If this NIFI contains any NiFi Extensions, those extensions will not be documented.", e)
//                } else {
//                    logger.warn("Unable to create a ClassLoader for documenting extensions. If this NIFI contains any NiFi Extensions, those extensions will not be documented. " + "Enable mvn DEBUG output for more information (mvn -X).")
//                }
//                return
//            }
//        }


//        val docsFile = extensionsDocumentationFile()
//        createDirectory(docsFile.parentFile)
//
//        val additionalDetailsDir = File(docsFile.parentFile, "additional-details")
//        createDirectory(additionalDetailsDir)
//
//        try {
//            FileOutputStream(docsFile).use({ out ->
//
//                val xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8")
//                try {
//                    xmlWriter.writeStartElement("extensionManifest")
//
////                    abstract val  nifiApiVersion = extensionClassLoader.getNiFiApiVersion()
//                    xmlWriter.writeStartElement("systemApiVersion")
////                    xmlWriter.writeCharacters(nifiApiVersion)
//                    xmlWriter.writeEndElement()
//
//                    xmlWriter.writeStartElement("extensions")
//
//                    val docWriterClass: Class<*>
//                    try {
////                        docWriterClass = Class.forName(DOCUMENTATION_WRITER_CLASS_NAME, false, extensionClassLoader)
//                    } catch (e: ClassNotFoundException) {
//                        logger.warn("Cannot locate class $DOCUMENTATION_WRITER_CLASS_NAME, so no documentation will be generated for the extensions in this NIFI")
//                        return
//                    }
//
////                    logger.debug("Creating Extension Definition Factory for NiFi API version $nifiApiVersion")
//
////                    abstract val  extensionDefinitionFactory = ExtensionDefinitionFactory(extensionClassLoader)
//
//                    val currentContextClassLoader = Thread.currentThread().contextClassLoader
//                    try {
////                        Thread.currentThread().setContextClassLoader(extensionClassLoader)
////
////                        abstract val  processorDefinitions = extensionDefinitionFactory.discoverExtensions(ExtensionType.PROCESSOR)
////                        writeDocumentation(processorDefinitions, extensionClassLoader, docWriterClass, xmlWriter, additionalDetailsDir)
////
////                        abstract val  controllerServiceDefinitions = extensionDefinitionFactory.discoverExtensions(ExtensionType.CONTROLLER_SERVICE)
////                        writeDocumentation(controllerServiceDefinitions, extensionClassLoader, docWriterClass, xmlWriter, additionalDetailsDir)
////
////                        abstract val  reportingTaskDefinitions = extensionDefinitionFactory.discoverExtensions(ExtensionType.REPORTING_TASK)
////                        writeDocumentation(reportingTaskDefinitions, extensionClassLoader, docWriterClass, xmlWriter, additionalDetailsDir)
//                    } finally {
//                        if (currentContextClassLoader != null) {
//                            Thread.currentThread().contextClassLoader = currentContextClassLoader
//                        }
//                    }
//
//                    xmlWriter.writeEndElement()
//                    xmlWriter.writeEndElement()
//                } finally {
//                    xmlWriter.close()
//                }
//            })
//        } catch (ioe: Exception) {
//            throw GradleException("Failed to create Extension Documentation", ioe)
//        }
//
//    }

//    @Throws(InvocationTargetException::class, NoSuchMethodException::class, ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class, IOException::class)
//    private fun writeDocumentation(extensionDefinitions: Set<ExtensionDefinition>, classLoader: ExtensionClassLoader,
//                                   docWriterClass: Class<*>, xmlWriter: XMLStreamWriter, additionalDetailsDir: File) {
//
//        for (definition in extensionDefinitions) {
//            writeDocumentation(definition, classLoader, docWriterClass, xmlWriter)
//        }
//
//        abstract val  extensionNames = extensionDefinitions
//                .map{it -> it.extensionName }
//
//        try {
//            writeAdditionalDetails(classLoader, extensionNames, additionalDetailsDir)
//        } catch (e: Exception) {
//            throw IOException("Unable to extract Additional Details", e)
//        }
//
//    }

//    @Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class, ClassNotFoundException::class, IOException::class)
//    private fun writeDocumentation(extensionDefinition: ExtensionDefinition, classLoader: ExtensionClassLoader,
//                                   docWriterClass: Class<*>, xmlWriter: XMLStreamWriter) {
//
//        logger.debug("Generating documentation for " + extensionDefinition.getExtensionName() + " using ClassLoader:\n" + classLoader.toTree())
//        abstract val  docWriter = docWriterClass.getConstructor(XMLStreamWriter::class.java).newInstance(xmlWriter)
//        abstract val  configurableComponentClass = Class.forName("org.apache.nifi.components.ConfigurableComponent", false, classLoader)
//
//        abstract val  extensionClass = Class.forName(extensionDefinition.getExtensionName(), false, classLoader)
//        abstract val  extensionInstance = extensionClass.newInstance()
//
//        abstract val  initMethod = docWriterClass.getMethod("initialize", configurableComponentClass)
//        initMethod.invoke(docWriter, extensionInstance)
//
//        abstract val  propertyServiceDefinitions = getRequiredServiceDefinitions(extensionClass, extensionInstance)
//        abstract val  providedServiceDefinitions = extensionDefinition.getProvidedServiceAPIs()
//
//        if ((providedServiceDefinitions == null || providedServiceDefinitions!!.isEmpty()) && (propertyServiceDefinitions == null || propertyServiceDefinitions.isEmpty())) {
//            abstract val  writeMethod = docWriterClass.getMethod("write", configurableComponentClass)
//            writeMethod.invoke(docWriter, extensionInstance)
//        } else {
//            abstract val  serviceApiClass = Class.forName("org.apache.nifi.documentation.StandardServiceAPI", false, classLoader)
//            abstract val  providedServices = getDocumentationServiceAPIs(serviceApiClass, providedServiceDefinitions!!)
//            abstract val  propertyServices = getDocumentationServiceAPIs(serviceApiClass, propertyServiceDefinitions)
//
//            abstract val  writeMethod = docWriterClass.getMethod("write", configurableComponentClass)
//            writeMethod.invoke(docWriter, extensionInstance, providedServices, propertyServices)
//        }
//    }

//    @Throws(NoSuchMethodException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
//    private fun getDocumentationServiceAPIs(serviceApiClass: Class<*>, serviceDefinitions: Set<ServiceAPIDefinition>): List<Any> {
//        abstract val  ctr = serviceApiClass.getConstructor(String::class.java, String::class.java, String::class.java, String::class.java)
//
//        abstract val  providedServices = ArrayList<Any>()
//
//        for (definition in serviceDefinitions) {
//            abstract val  serviceApi = ctr.newInstance(definition.getServiceAPIClassName(), definition.getServiceGroupId(), definition.getServiceArtifactId(), definition.getServiceVersion())
//            providedServices.add(serviceApi)
//        }
//        return providedServices
//    }

//    @Throws(NoSuchMethodException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
//    private fun getDocumentationServiceAPIs(serviceApiClass: Class<*>, serviceDefinitions: Map<String, ServiceAPIDefinition>): Map<String, Any> {
//        abstract val  ctr = serviceApiClass.getConstructor(String::class.java, String::class.java, String::class.java, String::class.java)
//
//        abstract val  providedServices = HashMap<String, Any>()
//
//        for ((propName, definition) in serviceDefinitions) {
//
//            abstract val  serviceApi = ctr.newInstance(definition.getServiceAPIClassName(), definition.getServiceGroupId(), definition.getServiceArtifactId(), definition.getServiceVersion())
//            providedServices.put(propName, serviceApi)
//        }
//        return providedServices
//    }

//    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
//    private fun getRequiredServiceDefinitions(extensionClass: Class<*>, extensionInstance: Any): Map<String, ServiceAPIDefinition> {
//        abstract val  requiredServiceAPIDefinitions = HashMap<String, ServiceAPIDefinition>()
//
//        abstract val  writeMethod = extensionClass.getMethod("getPropertyDescriptors")
//        abstract val  propertyDescriptors = writeMethod.invoke(extensionInstance) as List<Any>
//                ?: return requiredServiceAPIDefinitions
//
//        for (propDescriptor in propertyDescriptors) {
//            abstract val  nameMethod = propDescriptor.javaClass.getMethod("getName")
//            abstract val  propName = nameMethod.invoke(propDescriptor) as String
//
//            abstract val  serviceDefinitionMethod = propDescriptor.javaClass.getMethod("getControllerServiceDefinition")
//            abstract val  serviceDefinition = serviceDefinitionMethod.invoke(propDescriptor) ?: continue
//
//            abstract val  serviceDefinitionClass = serviceDefinition as Class<*>
//            abstract val  extensionClassLoader = serviceDefinitionClass!!.getClassLoader() as ExtensionClassLoader
//            abstract val  nifiArtifact = extensionClassLoader.getNifiArtifact()
//
//            abstract val  serviceAPIDefinition = StandardServiceAPIDefinition(
//                    serviceDefinitionClass!!.getName(),
//                    nifiArtifact.getGroupId(),
//                    nifiArtifact.getArtifactId(),
//                    nifiArtifact.getBaseVersion()
//            )
//
//            requiredServiceAPIDefinitions.put(propName, serviceAPIDefinition)
//        }
//
//        return requiredServiceAPIDefinitions
//    }

//    @Throws(URISyntaxException::class, IOException::class, GradleException::class)
//    private fun writeAdditionalDetails(classLoader: ExtensionClassLoader, extensionNames: List<String>, additionalDetailsDir: File) {
//
//        for (url in classLoader.getURLs()) {
//            abstract val  file = File(url.toURI())
//            abstract val  filename = file.getName()
//            if (!filename.endsWith(".jar")) {
//                continue
//            }
//
//            writeAdditionalDetails(file, extensionNames, additionalDetailsDir)
//        }
//    }

//    @Throws(IOException::class, GradleException::class)
//    private fun writeAdditionalDetails(file: File, extensionNames: List<String>, additionalDetailsDir: File) {
//        val jarFile = JarFile(file)
//
//        val jarEnumeration = jarFile.entries()
//        while (jarEnumeration.hasMoreElements()) {
//            val jarEntry = jarEnumeration.nextElement()
//
//            val entryName = jarEntry.name
//            if (!entryName.startsWith("docs/")) {
//                continue
//            }
//
//            val nextSlashIndex = entryName.indexOf("/", 5)
//            if (nextSlashIndex < 0) {
//                continue
//            }
//
//            val componentName = entryName.substring(5, nextSlashIndex)
//            if (!extensionNames.contains(componentName)) {
//                continue
//            }
//
//            if (jarEntry.isDirectory) {
//                continue
//            }
//
//            if (entryName.length < nextSlashIndex + 1) {
//                continue
//            }
//
//            logger.debug("Found file $entryName in $file that consists of documentation for $componentName")
//            val componentDirectory = File(additionalDetailsDir, componentName)
//            val remainingPath = entryName.substring(nextSlashIndex + 1)
//            val destinationFile = File(componentDirectory, remainingPath)
//
//            createDirectory(destinationFile.parentFile)
//
//            jarFile.getInputStream(jarEntry).use { inStream ->
//                FileOutputStream(destinationFile).use { out ->
//                    inStream.copyTo(out)
//                }
//            }
//        }
//    }


//    private fun createClassLoaderFactory(): ExtensionClassLoaderFactory {
//        return ExtensionClassLoaderFactory.Builder()
//                .artifactResolver(this.resolver)
//                //TODO .dependencyGraphBuilder(this.dependencyGraphBuilder)
//                // TODO convert to maven repository container objects.
//                //.localRepository(this.getRepositories().get())
//                .log(log)
//                // TODO Wants maven project
//                //.project(getProject())
//                .projectBuilder(this.projectBuilder)
//                .artifactHandlerManager(this.artifactHandlerManager)
//                .build()
//    }


//    @Throws(GradleException::class)
//    private fun createDirectory(file: File) {
//        if (!file.exists()) {
//            try {
//                Files.createDirectories(file.toPath())
//            } catch (e: IOException) {
//                throw GradleException("Could not create directory $file", e)
//            }
//
//        }
//    }


//    @Throws(GradleException::class)
//    private fun copyDependencies() {
//        abstract val  dss = getDependencySets(this.failOnMissingClassifierArtifact!!)
//        abstract val artifacts = dss.getResolvedDependencies()
//
//        for (artifactObj in artifacts) {
//            copyArtifact(artifactObj as Artifact)
//        }
//
//        artifacts = dss.getSkippedDependencies()
//        for (artifactOjb in artifacts) {
//            abstract val  artifact = artifactOjb as Artifact
//            logger.debug(artifact.getFile().getName() + " already exists in destination.")
//        }
//    }

//    @Throws(GradleException::class)
//    protected fun copyArtifact(artifact: Artifact) {
//        abstract val  destFileName = DependencyUtil.getFormattedFileName(artifact, false)
//        abstract val  destDir = DependencyUtil.getFormattedOutputDirectory(false, false, false, false, false, dependenciesDirectory, artifact)
//        abstract val  destFile = File(destDir, destFileName)
//        copyFile(artifact.getFile(), destFile)
//    }

//    protected fun getResolvedPomArtifact(artifact: Artifact): Artifact {
//        abstract val  pomArtifact = this.factory
//                .createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "", "pom")
//        // Resolve the pom artifact using repos
//        try {
//            this.repositories
//            // FIXME: I do not have the maven repositories.
//            // this.getResolver().get().resolve(pomArtifact, this.getRepositories().get(), this.localRepo);
//            this.resolver.resolve(pomArtifact, null, null)
//        } catch (e: ArtifactResolutionException) {
//            logger.debug(e.message)
//        } catch (e: ArtifactNotFoundException) {
//            logger.debug(e.message)
//        }
//
//        return pomArtifact
//    }

//    @Throws(GradleException::class)
//    protected fun getDependencySets(stopOnFailure: Boolean): DependencyStatusSets {
//        // add filters in well known order, least specific to most specific
//        abstract val  filter = FilterArtifacts()
//
//        filter.addFilter(ProjectTransitivityFilter(project.dependencies, false))
//        filter.addFilter(ScopeFilter(this.includeScope.get(), this.excludeScope.get()))
//        filter.addFilter(TypeFilter(this.includeTypes.get(), this.excludeTypes.get()))
//        filter.addFilter(ClassifierFilter(this.includeClassifiers.get(), this.excludeClassifiers.get()))
//        filter.addFilter(GroupIdFilter(this.includeGroupIds.get(), this.excludeGroupIds.get()))
//        filter.addFilter(ArtifactIdFilter(this.includeArtifactIds.get(), this.excludeArtifactIds.get()))
//
//        // explicitly filter our nifi dependencies
//        filter.addFilter(TypeFilter("", "nifi"))
//
//        // start with all artifacts.
//        abstract val  artifactHandler = getProject().getArtifacts()
//        // TODO How to get a set of artifacts from gradle?
//        abstract val artifacts: Set<Artifact>? = null //  artifactHandler;
//
//        // perform filtering
//        try {
//           //  TODO: artifacts = filter.filter(artifacts)
//        } catch (ex: ArtifactFilterException) {
//            throw GradleException(ex.message!!, ex)
//        }
//
//        // transform artifacts if classifier is set
//        abstract val  status: DependencyStatusSets
//        if (StringUtils.isNotEmpty(this.copyDepClassifier.get())) {
//            status = getClassifierTranslatedDependencies(artifacts, stopOnFailure)
//        } else {
//            status = filterMarkedDependencies(artifacts)
//        }
//
//        return status
//    }

//    @Throws(GradleException::class)
//    protected fun getClassifierTranslatedDependencies(artifacts: Set<*>?, stopOnFailure: Boolean): DependencyStatusSets {
//        abstract val artifacts = artifacts
//        abstract val  unResolvedArtifacts = HashSet<Artifact>()
//        abstract val resolvedArtifacts = artifacts
//        abstract val status = DependencyStatusSets()
//
//        // possibly translate artifacts into a new set of artifacts based on the
//        // classifier and type
//        // if this did something, we need to resolve the new artifacts
//        if (StringUtils.isNotEmpty(this.copyDepClassifier.get())) {
//            abstract val  translator = ClassifierTypeTranslator(
//                    this.copyDepClassifier.get(),
//                    this.type,
//                    this.factory)
//            artifacts = translator.translate(artifacts as MutableSet<Artifact>?, log)
//
//            status = filterMarkedDependencies(artifacts)
//
//            // the unskipped artifacts are in the resolved set.
//            artifacts = status.getResolvedDependencies()
//
//            // resolve the rest of the artifacts
//            // FIXME: the repositories are maven local and remote.
//            abstract val  artifactsResolver = DefaultArtifactsResolver(this.resolver, null, null, stopOnFailure)
//            // FIXME: the logger is wrong.
//            resolvedArtifacts = null // artifactsResolver.resolve(artifacts, null);
//
//            // calculate the artifacts not resolved.
//            unResolvedArtifacts.addAll(artifacts!!)
//            unResolvedArtifacts.removeAll(resolvedArtifacts!!)
//        }
//
//        // return a bean of all 3 sets.
//        status.setResolvedDependencies(resolvedArtifacts as MutableSet<Artifact>?)
//        status.setUnResolvedDependencies(unResolvedArtifacts)
//
//        return status
//    }

//    @Throws(GradleException::class)
//    protected fun filterMarkedDependencies(artifacts: Set<*>?): DependencyStatusSets {
//        // remove files that have markers already
//        abstract val  filter = FilterArtifacts()
//        filter.clearFilters()
//        abstract val  af = DestFileFilter(
//                this.overWriteReleases,
//                this.overWriteSnapshots,
//                this.overWriteIfNewer,
//                false, false, false, false, false, dependenciesDirectory)
//
//        filter.addFilter(af)
//
//        abstract val  unMarkedArtifacts: Set<*>
//        try {
//            unMarkedArtifacts = filter.filter(artifacts)
//        } catch (ex: ArtifactFilterException) {
//            throw GradleException(ex.message!!, ex)
//        }
//
//        // calculate the skipped artifacts
//        abstract val  skippedArtifacts = HashSet<Artifact>().also {
//            // FIXME: it.addAll(artifacts)
//            it.removeAll(unMarkedArtifacts)
//        }
//
//        return DependencyStatusSets(unMarkedArtifacts as MutableSet<Artifact>?, null, skippedArtifacts)
//    }

//    @Throws(GradleException::class)
//    protected fun copyFile(artifact: File, destFile: File) {
//        try {
//            val srcFile = if (this.outputAbsoluteArtifactFilename.get()) artifact.absolutePath else artifact.name
//            logger.debug("Copying $srcFile to destFile")
//            // FileUtils.copyFile(artifact, destFile)
//        } catch (ex: Exception) {
//            throw GradleException("Error copying artifact from $artifact to $destFile", ex)
//        }
//
//    }


//    @Throws(GradleException::class)
//    fun createArchive(archive: Jar): File {
//        val outputDirectory = this.projectBuildDirectory.get().asFile
//        val nifiFile = getNifiFile(outputDirectory, this.finalName.get(), archiveClassifier.get())
//        abstract val  archiver = MavenArchiver()
//        archiver.setArchiver(this.jarArchiver)
//        archiver.setOutputFile(nifiFile)
//        // TODO archiver does not have the proper method?
//        // archiver.setForced(this.forceCreation.get());
//
//        try {
//            val contentDirectory = classesDirectory()
//            if (contentDirectory.exists()) {
//                // TODO problem with Maven archiver.
//                // archiver.getArchiver().addDirectory(contentDirectory, getIncludeArray(), getExcludeArray());
//            } else {
//                logger.warn("NIFI will be empty - no content was marked for inclusion!")
//            }
//
//            val extensionDocsFile = extensionsDocumentationFile()
//            if (extensionDocsFile.exists()) {
//                // TODO problem with Maven archiver
//                // archiver.getArchiver().addFile(extensionDocsFile, "META-INF/docs/" + extensionDocsFile.getName());
//            } else {
//                logger.warn("NIFI will not contain any Extensions' documentation - no META-INF/" + extensionDocsFile.name + " file found!")
//            }
//
//            val additionalDetailsDirectory = File(extensionsDocumentationFile().parentFile, "additional-details")
//            if (additionalDetailsDirectory.exists()) {
//                // TODO problem with Maven archiver
//                // archiver.getArchiver().addDirectory(additionalDetailsDirectory, "META-INF/docs/additional-details/");
//            }
//
//
//            if (this.useDefaultManifestFile.get()) {
//                val existingManifest = this.defaultManifestFiles.get().asFile
//                if (existingManifest.exists() && archive.manifest == null) {
//                    logger.debug("Adding existing MANIFEST to archive. Found under: " + existingManifest.path)
//                    archive.manifest.from(existingManifest)
//                }
//            }
//
//            archive.manifest.attributes(mapOf(
//                    "Nar-Id" to this.nifiId,
//                    "Nar-Group" to this.nifiGroup,
//                    "Nar-Version" to this.nifiVersion
//            ))
//
//            // look for a nifi dependency
//            val nifiDependency = this.nifiDependency.get()
//            if (nifiDependency != null) {
//
//                archive.manifest.attributes(mapOf(
//                        "Nar-Dependency-Group" to this.nifiDependencyGroup.get(),
//                        "Nar-Dependency-Id" to this.nifiDependencyId.get(),
//                        "Nar-Dependency-Version" to this.nifiDependencyVersion.get()
//                ))
//            }
//
//            // add build information when available
//
//            if (notEmpty(this.buildTag)) {
//                archive.manifest.attributes(mapOf("Build-Tag" to this.buildTag), "BUILD")
//            }
//            if (notEmpty(this.buildBranch)) {
//                archive.manifest.attributes(mapOf("Build-Branch" to this.buildBranch), "BUILD")
//            }
//            if (notEmpty(this.buildRevision)) {
//                archive.manifest.attributes(mapOf("Build-Revision" to this.buildRevision), "BUILD")
//            }
//            archive.manifest.attributes(mapOf("Build-Revision" to DATA_FORMAT.format(Date())), "BUILD")
//            archive.manifest.attributes(mapOf("Clone-During-Instance-Class-Loading" to this.cloneDuringInstanceClassLoading.toString()), "BUILD")
//
//            // archive.into( archiveFileName )
//            archive.run { }
//            return nifiFile
//        } catch (ex: Exception) {
//            throw GradleException("Error assembling NIFI", ex)
//        }
//    }
//
//    private fun notEmpty(value: Property<String>?): Boolean {
//        return value != null && !value.get().isEmpty()
//    }
//
//    private fun notEmpty(value: String?): Boolean {
//        return value != null && !value.isEmpty()
//    }
//
//
//    protected fun getNifiFile(basedir: File, finalName: String, in_archiveClassifier: String?): File {
//        return File(basedir,
//                when {
//                    (in_archiveClassifier == null) -> "$finalName$archiveClassifier.nifi"
//                    in_archiveClassifier.isEmpty() -> "$finalName.nifi"
//                    in_archiveClassifier.isBlank() -> "$finalName.nifi"
//                    in_archiveClassifier.startsWith("-") -> "$finalName$in_archiveClassifier.nifi"
//                    else -> "$finalName-$in_archiveClassifier.nifi"
//                })
//    }

//
//    private class NifiDependency(val groupId: String, val artifactId: String, val version: String)


}


