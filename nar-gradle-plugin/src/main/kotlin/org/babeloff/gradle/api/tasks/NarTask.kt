package org.babeloff.gradle.api.tasks


import groovy.lang.Closure
import org.apache.log4j.LogManager
import org.babeloff.gradle.api.annotations.Parameter
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.CopySpec
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.jvm.tasks.Jar
import org.gradle.util.ConfigureUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.jar.JarFile
import javax.xml.stream.XMLOutputFactory

/**
 * Assembles a NAR archive.
 *
 * The specification for which can be found at:
 * https://gitbox.apache.org/repos/asf?p=nifi-maven.git
 * https://gitbox.apache.org/repos/asf?p=nifi-maven.git;a=blob;f=src/main/java/org/apache/nifi/NarMojo.java
 *
 * @author Fred Eisele
 */
open class NarTask : DefaultTask() {

    companion object {
        private val logger = LogManager.getLogger(NarTask::class.java)

        val NAR_EXTENSION = "nar"

        val SERVICES_DIRECTORY = "META-INF/services/"
        val BUNDLE_DIRECTORY = "META-INF/bundled-dependencies/"
        val DOCUMENTATION_WRITER_CLASS_NAME = "org.apache.nifi.documentation.xml.XmlDocumentationWriter"

        val DEFAULT_EXCLUDES = listOf("**/package.html")
        val DEFAULT_INCLUDES = listOf("**/**")

        val BUILD_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        val DATA_FORMAT = SimpleDateFormat(BUILD_TIMESTAMP_FORMAT)
        val VERSION_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    }

    init {
        logger.info("initializing nar task")
    }

    /**
     * POM
     */
//    @Parameter(defaultValue = "\${project}", readonly = true, required = true)
//    var project: Project

//    @Parameter(defaultValue = "${session}", readonly = true, required = true)
//    public MavenSession session;

    /**
     * List of files to include. Specified as fileset patterns.
     */
    @Parameter(property = "includes")
    @Input
    val includeList = project.objects.listProperty(String::class.java)
    init {
        logger.info("initializing nar task includes")
        includeList.value(DEFAULT_INCLUDES)
    }
    /**
     * List of files to exclude. Specified as fileset patterns.
     */
    @Parameter(property = "excludes")
    @Input
    val excludeList = project.objects.listProperty(String::class.java)
    init {
        excludeList.value(DEFAULT_EXCLUDES)
    }
    /**
     * Name of the generated NAR.
     *
     */
    @Parameter(alias = "narName", property = "nar.finalName",
            defaultValue = "\${project.getName()}", required = true)
    @Input
    var finalName: String = project.name

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
//    var archive: MavenArchiveConfiguration = MavenArchiveConfiguration()

    /**
     * Path to the default MANIFEST file to use. It will be used if
     * `useDefaultManifestFile` is set to `true`.
     *
     */
    @Parameter(property = "defaultManifestFiles",
            defaultValue = "\${project.build.outputDirectory}/META-INF/MANIFEST.MF", readonly = true, required = true)
    @OutputFile
    val defaultManifestFiles  = project.objects.fileProperty()
    init {
        logger.info("initializing nar task defaultManifestFiles")
        defaultManifestFiles.set(File(project.buildDir.canonicalFile,  "META-INF/MANIFEST.MF").canonicalFile)
    }

    /**
     * Set this to `true` to enable the use of the
     * `defaultManifestFile`.
     *
     * @since 2.2
     */
    @Parameter(property = "nar.useDefaultManifestFile", defaultValue = "false")
    @Input
    var useDefaultManifestFile = false

//    @Component
//    val projectHelper: MavenProjectHelper = MavenProjectHelperDefault()

    /**
     * Whether creating the archive should be forced.
     *
     */
    @Parameter(property = "nar.forceCreation", defaultValue = "false")
    @Input
    var forceCreation = false

    /**
     * Classifier to add to the artifact generated.
     * If given, the artifact will be an attachment instead.
     */
    @Parameter(property = "classifier")
    @Input
    var archiveClassifier: String? = "nar"


//    @Component
//    val installer: ArtifactInstaller =  ArtifactInstallerDefault()


//    @Component
//    val repositoryFactory: ArtifactRepositoryFactory = ArtifactRepositoryFactoryDefault()

    /**
     * This only applies if the classifier parameter is used.
     *
     */
    @Parameter(property = "mdep.failOnMissingClassifierArtifact", defaultValue = "true", required = false)
    @Input
    var failOnMissingClassifierArtifact = true

    /**
     * Comma Separated list of Types to include. Empty String indicates include
     * everything (default).
     *
     */
    @Parameter(property = "includeTypes", required = false)
    @Input
    val includeTypes = project.objects.property(String::class.java)

    /**
     * Comma Separated list of Types to exclude. Empty String indicates don't
     * exclude anything (default).
     *
     */
    @Parameter(property = "excludeTypes", required = false)
    @Input
    val excludeTypes = project.objects.property(String::class.java)

    /**
     * Scope to include. An Empty string indicates all scopes (default).
     *
     */
    @Parameter(property = "includeScope", required = false)
    @Input
    val includeScope = project.objects.property(String::class.java)

    /**
     * Scope to exclude. An Empty string indicates no scopes (default).
     *
     */
    @Parameter(property = "excludeScope", required = false)
    @Input
    val excludeScope = project.objects.property(String::class.java)

    /**
     * Comma Separated list of Classifiers to include. Empty String indicates
     * include everything (default).
     *
     */
    @Parameter(property = "includeClassifiers", required = false)
    @Input
    val includeClassifiers = project.objects.property(String::class.java)

    /**
     * Comma Separated list of Classifiers to exclude. Empty String indicates
     * don't exclude anything (default).
     *
     */
    @Parameter(property = "excludeClassifiers", required = false)
    @Input
    val excludeClassifiers = project.objects.property(String::class.java)

    /**
     * Specify classifier to look for. Example: sources
     *
     */
    @Parameter(property = "classifier", required = false)
    @Input
    val copyDepClassifier = project.objects.property(String::class.java)

    /**
     * Specify type to look for when constructing artifact based on classifier.
     * Example: java-source,jar,war, nar
     *
     */
    @Parameter(property = "type", required = true, defaultValue = "nar")
    @Input
    var type = "nar"

    /**
     * Comma separated list of Artifact names too exclude.
     *
     */
    @Parameter(property = "excludeArtifacts", required = false)
    @Input
    val excludeArtifactIds = project.objects.property(String::class.java)

    /**
     * Comma separated list of Artifact names to include.
     *
     */
    @Parameter(property = "includeArtifacts", required = false)
    @Input
    val includeArtifactIds = project.objects.property(String::class.java)

    /**
     * Comma separated list of GroupId Names to exclude.
     *
     */
    @Parameter(property = "excludeArtifacts", required = false)
    @Input
    val excludeGroupIds = project.objects.property(String::class.java)

    /**
     * Comma separated list of GroupIds to include.
     *
     */
    @Parameter(property = "includeGroupIds", required = false)
    @Input
    val includeGroupIds = project.objects.property(String::class.java)

    /**
     * Directory to store flag files
     *
     */
    @Parameter(property = "markersDirectory", required = true,
            defaultValue = "\${project.build.directory}/dependency-maven-plugin-markers")
    @OutputDirectory
    val markersDirectory = project.objects.directoryProperty()
    init {
        logger.info("initializing nar task markers directory")
        markersDirectory.set( File(project.buildDir, "dependency-maven-plugin-markers").canonicalFile )
    }

    /**
     * Overwrite release artifacts
     *
     */
    @Parameter(property = "overWriteReleases", required = true)
    @Input
    var overWriteReleases = false

    /**
     * Overwrite snapshot artifacts
     *
     */
    @Parameter(property = "overWriteSnapshots", required = true)
    @Input
    var overWriteSnapshots = false

    /**
     * Overwrite artifacts that don't exist or are older than the source.
     *
     */
    @Parameter(property = "overWriteIfNewer", required = true, defaultValue = "true")
    @Input
    var overWriteIfNewer = true

    @Parameter(property = "projectBuildDirectory", required = true, defaultValue = "\${project.build.directory}")
    @OutputDirectory
    val projectBuildDirectory = project.objects.directoryProperty()
    init {
        projectBuildDirectory.set(project.buildDir.canonicalFile)
    }

    /**
     * Used to look up Artifacts in the remote repository.
     */
//    @Component
//    val factory: ArtifactFactory = ArtifactFactoryDefault()

    /**
     * Used to look up Artifacts in the remote repository.
     *
     */
//    @Component
//    val resolver: ArtifactResolver = ArtifactResolverDefault()

    /**
     * Container of Repositories used by the resolver
     *
     */
    @Parameter(property = "project.remoteArtifactRepositories", required = true, readonly = true)
    @Input
    var repositories = project.repositories

    /**
     * To look up Archiver/UnArchiver implementations
     *
     */
//    @Component
//    var archiverManager: ArchiverManager = ArchiverManagerDefault()

    /**
     * Contains the full list of projects in the reactor.
     *
     */
    @Parameter(property = "reactorProjects", required = true, readonly = true)
    @Input
    val reactorProjects = project.objects.listProperty(String::class.java)

    /**
     * If the plugin should be silent.
     *
     */
    @Parameter(property = "silent", required = false, defaultValue = "false")
    @Input
    var silent = false

    /**
     * The dependency tree builder to use for verbose output.
     */
//    @Component
//    val dependencyGraphBuilder: DependencyGraphBuilder = DependencyGraphBuilder(
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
//    val artifactHandlerManager: ArtifactHandlerManager = ArtifactHandlerManagerDefault()

    /**
     * Output absolute filename for resolved artifacts
     *
     */
    @Parameter(property = "outputAbsoluteArtifactFilename", defaultValue = "false", required = false)
    @Input
    var outputAbsoluteArtifactFilename = false

    /**
     * The values to use for populating the Nar-Group, Nar-Id, and Nar-Version in the MANIFEST file.
     * By default these values will be set to the standard Maven project equivalents,
     * but they may be overridden through properties.
     *
     * For example if the build.gradle.kts for the nifi-test-nar contained the following:
     *
     * groupId = "org.apache.nifi"
     * artifactId = nifi-test-nar"
     * version = "1.0"
     *
     * nar {
     * narGroup = "org.apache.nifi.overridden"
     * narId = "nifi-overridden-test-nar"
     * narVersion = "2.0"
     * }
     *
     * It would produce a MANIFEST with:
     *
     * Nar-Id: nifi-overridden-test-nar
     * Nar-Group: org.apache.nifi.overridden
     * Nar-Version: 2.0
     *
     */

    @Parameter(property = "narGroup", defaultValue = "\${project.groupId}", required = true)
    @Input
    var narGroup: String

    @Parameter(property = "narId", defaultValue = "\${project.artifactId}", required = true)
    @Input
    var narId: String

    @Parameter(property = "narVersion", defaultValue = "\${project.version}", required = true)
    @Input
    var narVersion: String

    init {
        narGroup = project.group.toString()
        narId = project.displayName
        narVersion = project.version.toString()
    }

    @Parameter(property = "narDependencyGroup", required = false, defaultValue = "null")
    @Input
    val narDependencyGroup = project.objects.property(String::class.java)

    @Parameter(property = "narDependencyId", required = false, defaultValue = "null")
    @Input
    val narDependencyId = project.objects.property(String::class.java)

    @Parameter(property = "narDependencyVersion", required = false, defaultValue = "null")
    @Input
    val narDependencyVersion = project.objects.property(String::class.java)
    init {
        logger.info("initializing nar task dependency")
        narDependencyGroup.set(null)
        narDependencyId.set(null)
        narDependencyVersion.set(null)
    }


    /**
     * Build info to be populated in MANIFEST.
     */

    @Parameter(property = "buildTag", defaultValue = "\${project.scm.tag}", required = false)
    @Input
    var buildTag: String = ""

    @Parameter(property = "buildBranch", defaultValue = "\${buildBranch}", required = false)
    @Input
    var buildBranch: String = ""

    @Parameter(property = "buildRevision", defaultValue = "\${buildRevision}", required = false)
    @Input
    var buildRevision: String = VERSION_FORMAT.format(LocalDateTime.now())

    /**
     * Allows a NAR to specify if it's resources should be cloned when a component that depends on this NAR
     * is performing class loader isolation.
     */
    @Parameter(property = "cloneDuringInstanceClassLoading", defaultValue = "false", required = false)
    @Input
    var cloneDuringInstanceClassLoading = false


    @Parameter(property = "enforceDocGeneration", defaultValue = "false", required = false)
    @Input
    var enforceDocGeneration = false

    /**
     * The [RepositorySystemSession] used for obtaining the local and remote artifact repositories.
     */
    //@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    //private RepositorySystemSession getRepoSession();


    /**
     * The [ProjectBuilder] used to generate the [MavenProject]
     * for the nar artifact the dependency tree is being generated for.
     */
//    @Component
//    val projectBuilder: ProjectBuilder = ProjectBuilderDefault()


    /**
     * The following functions were lifted from
     *
     * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPluginConvention.java
     *
     * @return
     */

    private val extensionsDocumentationFile: File
        get() {
            val directory = File(this.projectBuildDirectory.get().getAsFile(), "META-INF/docs")
            return File(directory, "extension-manifest.xml")
        }

    private val classesDirectory: File
        get() {
            val outputDirectory = this.projectBuildDirectory.get().getAsFile()
            return File(outputDirectory, "classes")
        }

    private val dependenciesDirectory: File
        get() = File(classesDirectory, "META-INF/bundled-dependencies")

    private
    // get nar dependencies
    // start with all artifacts.
    // FIXME: how do I get a set of the artifacts? dependencies?
    // artifactHandler;
    // perform filtering
    // ensure there is a single nar dependency
    val narDependency: NarDependency?
        @Throws(GradleException::class)
        get() {
            var narDependency: NarDependency? = null
//            val filter = FilterArtifacts()
//            filter.addFilter(TypeFilter("nar", ""))
            val artifactHandler = project.artifacts
//            var artifacts: Set<Artifact>? = artifactHandler.
////            try {
////                // TODO: artifacts = filter.filter(artifacts).orEmpty()
//////            } catch (ex: ArtifactFilterException) {
//////                throw GradleException(ex.message!!, ex)
////            }
//
//            if (artifacts.size > 1) {
//                throw GradleException("Each NAR represents a ClassLoader. A NAR dependency allows that NAR's ClassLoader to be " + "used as the parent of this NAR's ClassLoader. As a result, only a single NAR dependency is allowed.")
//            }
//            if (artifacts.size == 1) {
//                val artifact = artifacts.iterator().next() as Artifact
//                narDependency = NarDependency(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion())
//            }

            return narDependency
        }


    /**
     *
     * The specification for NAR can be found here:
     * * http://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#nars
     * * http://maven-nar.github.io/
     *
     */
    @TaskAction
    @Throws(GradleException::class)
    fun execute() {

        val archive = Jar()

        logger.info("executing nar task")

        val metaInf = archive.metaInf
        archive.archiveExtension.set(NAR_EXTENSION)
        val rootSpec = archive.rootSpec

         /**
         * Specify which dependencies are to be bundled in the NAR.
         *
         * The bundled-dependencies contains the jar files that will
         * be used by the processor and accompanying controller services
         * (if the NAR contains a controller service).
         * These jar files will be loaded in the ClassLoader that is dedicated to that processor.
         */
        logger.trace("configure bundled dependencies()")

        // TODO
//        getMetaInf().into(BUNDLE_DIRECTORY, { spec ->
//            spec.from({ null }//            getProject().getConfigurations().runtimeClasspath.get()
//                    //                            .filter { it.name.endsWith("jar") }
//                    //                    .map { it }
//                    as Callable<Iterable<File>>)
//        })


        //        configureManifest();
        //        configureParentNarManifestEntry();

        copyDependencies()

        try {
            generateDocumentation()
        } catch (t: Throwable) {

            if (this.enforceDocGeneration) {
                // Catch Throwable in case a linkage error such as NoClassDefFoundError occurs
                logger.error("Could not generate extensions' documentation", t)
                throw t
            } else {
                logger.warn("Could not generate extensions' documentation", t)
            }
        }

        makeNar(archive)
    }


    /**
     * Adds some content to the `WEB-INF` directory for this NAR archive.
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
     * Adds some content to the `WEB-INF` directory for this NAR archive.
     *
     *
     * The given action is executed to configure a [CopySpec].
     *
     * @param configureAction The action to execute
     * @return The newly created `CopySpec`.
     * @since 3.5
     */
//    fun nifiInf(configureAction: Action<in CopySpec>): CopySpec {
//        val nifiInf = getMetaInf()
//        configureAction.execute(nifiInf)
//        return nifiInf
//    }


//
//    /**
//     * Adds files to the classpath to include in the NAR archive.
//     *
//     * @param classpath The files to add. These are evaluated as per [org.gradle.api.Project.files]
//     */
//    fun classpath(vararg classpath: Any) {
//        val oldClasspath = getClasspath()
//        this.classpath = getProject().files(if (oldClasspath != null) oldClasspath else ArrayList(), classpath)
//    }

    @Throws(GradleException::class)
    private fun generateDocumentation() {
        logger.info("Generating documentation for NiFi extensions in the NAR...")

        // Create the ClassLoader for the NAR
//        val classLoaderFactory = createClassLoaderFactory()

//        val extensionClassLoader: ExtensionClassLoader
//        try {
//            extensionClassLoader = classLoaderFactory.createExtensionClassLoader()
//        } catch (e: Exception) {
//            if (this.enforceDocGeneration!!) {
//                throw GradleException("Failed to create Extension Documentation", e)
//            } else {
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Unable to create a ClassLoader for documenting extensions. If this NAR contains any NiFi Extensions, those extensions will not be documented.", e)
//                } else {
//                    logger.warn("Unable to create a ClassLoader for documenting extensions. If this NAR contains any NiFi Extensions, those extensions will not be documented. " + "Enable mvn DEBUG output for more information (mvn -X).")
//                }
//                return
//            }
//        }


        val docsFile = extensionsDocumentationFile
        createDirectory(docsFile.getParentFile())

        val additionalDetailsDir = File(docsFile.getParentFile(), "additional-details")
        createDirectory(additionalDetailsDir)

        try {
            FileOutputStream(docsFile).use({ out ->

                val xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8")
                try {
                    xmlWriter.writeStartElement("extensionManifest")

//                    val nifiApiVersion = extensionClassLoader.getNiFiApiVersion()
                    xmlWriter.writeStartElement("systemApiVersion")
//                    xmlWriter.writeCharacters(nifiApiVersion)
                    xmlWriter.writeEndElement()

                    xmlWriter.writeStartElement("extensions")

                    val docWriterClass: Class<*>
                    try {
//                        docWriterClass = Class.forName(DOCUMENTATION_WRITER_CLASS_NAME, false, extensionClassLoader)
                    } catch (e: ClassNotFoundException) {
                        logger.warn("Cannot locate class $DOCUMENTATION_WRITER_CLASS_NAME, so no documentation will be generated for the extensions in this NAR")
                        return
                    }

//                    logger.debug("Creating Extension Definition Factory for NiFi API version $nifiApiVersion")

//                    val extensionDefinitionFactory = ExtensionDefinitionFactory(extensionClassLoader)

                    val currentContextClassLoader = Thread.currentThread().getContextClassLoader()
                    try {
//                        Thread.currentThread().setContextClassLoader(extensionClassLoader)
//
//                        val processorDefinitions = extensionDefinitionFactory.discoverExtensions(ExtensionType.PROCESSOR)
//                        writeDocumentation(processorDefinitions, extensionClassLoader, docWriterClass, xmlWriter, additionalDetailsDir)
//
//                        val controllerServiceDefinitions = extensionDefinitionFactory.discoverExtensions(ExtensionType.CONTROLLER_SERVICE)
//                        writeDocumentation(controllerServiceDefinitions, extensionClassLoader, docWriterClass, xmlWriter, additionalDetailsDir)
//
//                        val reportingTaskDefinitions = extensionDefinitionFactory.discoverExtensions(ExtensionType.REPORTING_TASK)
//                        writeDocumentation(reportingTaskDefinitions, extensionClassLoader, docWriterClass, xmlWriter, additionalDetailsDir)
                    } finally {
                        if (currentContextClassLoader != null) {
                            Thread.currentThread().setContextClassLoader(currentContextClassLoader)
                        }
                    }

                    xmlWriter.writeEndElement()
                    xmlWriter.writeEndElement()
                } finally {
                    xmlWriter.close()
                }
            })
        } catch (ioe: Exception) {
            throw GradleException("Failed to create Extension Documentation", ioe)
        }

    }

//    @Throws(InvocationTargetException::class, NoSuchMethodException::class, ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class, IOException::class)
//    private fun writeDocumentation(extensionDefinitions: Set<ExtensionDefinition>, classLoader: ExtensionClassLoader,
//                                   docWriterClass: Class<*>, xmlWriter: XMLStreamWriter, additionalDetailsDir: File) {
//
//        for (definition in extensionDefinitions) {
//            writeDocumentation(definition, classLoader, docWriterClass, xmlWriter)
//        }
//
//        val extensionNames = extensionDefinitions
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
//        val docWriter = docWriterClass.getConstructor(XMLStreamWriter::class.java).newInstance(xmlWriter)
//        val configurableComponentClass = Class.forName("org.apache.nifi.components.ConfigurableComponent", false, classLoader)
//
//        val extensionClass = Class.forName(extensionDefinition.getExtensionName(), false, classLoader)
//        val extensionInstance = extensionClass.newInstance()
//
//        val initMethod = docWriterClass.getMethod("initialize", configurableComponentClass)
//        initMethod.invoke(docWriter, extensionInstance)
//
//        val propertyServiceDefinitions = getRequiredServiceDefinitions(extensionClass, extensionInstance)
//        val providedServiceDefinitions = extensionDefinition.getProvidedServiceAPIs()
//
//        if ((providedServiceDefinitions == null || providedServiceDefinitions!!.isEmpty()) && (propertyServiceDefinitions == null || propertyServiceDefinitions.isEmpty())) {
//            val writeMethod = docWriterClass.getMethod("write", configurableComponentClass)
//            writeMethod.invoke(docWriter, extensionInstance)
//        } else {
//            val serviceApiClass = Class.forName("org.apache.nifi.documentation.StandardServiceAPI", false, classLoader)
//            val providedServices = getDocumentationServiceAPIs(serviceApiClass, providedServiceDefinitions!!)
//            val propertyServices = getDocumentationServiceAPIs(serviceApiClass, propertyServiceDefinitions)
//
//            val writeMethod = docWriterClass.getMethod("write", configurableComponentClass)
//            writeMethod.invoke(docWriter, extensionInstance, providedServices, propertyServices)
//        }
//    }

//    @Throws(NoSuchMethodException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
//    private fun getDocumentationServiceAPIs(serviceApiClass: Class<*>, serviceDefinitions: Set<ServiceAPIDefinition>): List<Any> {
//        val ctr = serviceApiClass.getConstructor(String::class.java, String::class.java, String::class.java, String::class.java)
//
//        val providedServices = ArrayList<Any>()
//
//        for (definition in serviceDefinitions) {
//            val serviceApi = ctr.newInstance(definition.getServiceAPIClassName(), definition.getServiceGroupId(), definition.getServiceArtifactId(), definition.getServiceVersion())
//            providedServices.add(serviceApi)
//        }
//        return providedServices
//    }

//    @Throws(NoSuchMethodException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
//    private fun getDocumentationServiceAPIs(serviceApiClass: Class<*>, serviceDefinitions: Map<String, ServiceAPIDefinition>): Map<String, Any> {
//        val ctr = serviceApiClass.getConstructor(String::class.java, String::class.java, String::class.java, String::class.java)
//
//        val providedServices = HashMap<String, Any>()
//
//        for ((propName, definition) in serviceDefinitions) {
//
//            val serviceApi = ctr.newInstance(definition.getServiceAPIClassName(), definition.getServiceGroupId(), definition.getServiceArtifactId(), definition.getServiceVersion())
//            providedServices.put(propName, serviceApi)
//        }
//        return providedServices
//    }

//    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
//    private fun getRequiredServiceDefinitions(extensionClass: Class<*>, extensionInstance: Any): Map<String, ServiceAPIDefinition> {
//        val requiredServiceAPIDefinitions = HashMap<String, ServiceAPIDefinition>()
//
//        val writeMethod = extensionClass.getMethod("getPropertyDescriptors")
//        val propertyDescriptors = writeMethod.invoke(extensionInstance) as List<Any>
//                ?: return requiredServiceAPIDefinitions
//
//        for (propDescriptor in propertyDescriptors) {
//            val nameMethod = propDescriptor.javaClass.getMethod("getName")
//            val propName = nameMethod.invoke(propDescriptor) as String
//
//            val serviceDefinitionMethod = propDescriptor.javaClass.getMethod("getControllerServiceDefinition")
//            val serviceDefinition = serviceDefinitionMethod.invoke(propDescriptor) ?: continue
//
//            val serviceDefinitionClass = serviceDefinition as Class<*>
//            val extensionClassLoader = serviceDefinitionClass!!.getClassLoader() as ExtensionClassLoader
//            val narArtifact = extensionClassLoader.getNarArtifact()
//
//            val serviceAPIDefinition = StandardServiceAPIDefinition(
//                    serviceDefinitionClass!!.getName(),
//                    narArtifact.getGroupId(),
//                    narArtifact.getArtifactId(),
//                    narArtifact.getBaseVersion()
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
//            val file = File(url.toURI())
//            val filename = file.getName()
//            if (!filename.endsWith(".jar")) {
//                continue
//            }
//
//            writeAdditionalDetails(file, extensionNames, additionalDetailsDir)
//        }
//    }

    @Throws(IOException::class, GradleException::class)
    private fun writeAdditionalDetails(file: File, extensionNames: List<String>, additionalDetailsDir: File) {
        val jarFile = JarFile(file)

        val jarEnumeration = jarFile.entries()
        while (jarEnumeration.hasMoreElements()) {
            val jarEntry = jarEnumeration.nextElement()

            val entryName = jarEntry.getName()
            if (!entryName.startsWith("docs/")) {
                continue
            }

            val nextSlashIndex = entryName.indexOf("/", 5)
            if (nextSlashIndex < 0) {
                continue
            }

            val componentName = entryName.substring(5, nextSlashIndex)
            if (!extensionNames.contains(componentName)) {
                continue
            }

            if (jarEntry.isDirectory()) {
                continue
            }

            if (entryName.length < nextSlashIndex + 1) {
                continue
            }

            logger.debug("Found file $entryName in $file that consists of documentation for $componentName")
            val componentDirectory = File(additionalDetailsDir, componentName)
            val remainingPath = entryName.substring(nextSlashIndex + 1)
            val destinationFile = File(componentDirectory, remainingPath)

            createDirectory(destinationFile.getParentFile())

            jarFile.getInputStream(jarEntry).use { inStream ->
                FileOutputStream(destinationFile).use { out ->
                    inStream.copyTo(out)
                }
            }
        }
    }


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


    @Throws(GradleException::class)
    private fun createDirectory(file: File) {
        if (!file.exists()) {
            try {
                Files.createDirectories(file.toPath())
            } catch (e: IOException) {
                throw GradleException("Could not create directory $file", e)
            }

        }
    }


    @Throws(GradleException::class)
    private fun copyDependencies() {
//        val dss = getDependencySets(this.failOnMissingClassifierArtifact!!)
//        var artifacts = dss.getResolvedDependencies()
//
//        for (artifactObj in artifacts) {
//            copyArtifact(artifactObj as Artifact)
//        }
//
//        artifacts = dss.getSkippedDependencies()
//        for (artifactOjb in artifacts) {
//            val artifact = artifactOjb as Artifact
//            logger.debug(artifact.getFile().getName() + " already exists in destination.")
//        }
    }

//    @Throws(GradleException::class)
//    protected fun copyArtifact(artifact: Artifact) {
//        val destFileName = DependencyUtil.getFormattedFileName(artifact, false)
//        val destDir = DependencyUtil.getFormattedOutputDirectory(false, false, false, false, false, dependenciesDirectory, artifact)
//        val destFile = File(destDir, destFileName)
//        copyFile(artifact.getFile(), destFile)
//    }

//    protected fun getResolvedPomArtifact(artifact: Artifact): Artifact {
//        val pomArtifact = this.factory
//                .createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "", "pom")
//        // Resolve the pom artifact using repos
//        try {
//            this.repositories
//            // FIXME: I do not have the maven repositories.
//            // this.getResolver().get().resolve(pomArtifact, this.getRepositories().get(), this.localRepo);
//            this.resolver.resolve(pomArtifact, null, null)
//        } catch (e: ArtifactResolutionException) {
//            logger.info(e.message)
//        } catch (e: ArtifactNotFoundException) {
//            logger.info(e.message)
//        }
//
//        return pomArtifact
//    }

//    @Throws(GradleException::class)
//    protected fun getDependencySets(stopOnFailure: Boolean): DependencyStatusSets {
//        // add filters in well known order, least specific to most specific
//        val filter = FilterArtifacts()
//
//        filter.addFilter(ProjectTransitivityFilter(project.dependencies, false))
//        filter.addFilter(ScopeFilter(this.includeScope.get(), this.excludeScope.get()))
//        filter.addFilter(TypeFilter(this.includeTypes.get(), this.excludeTypes.get()))
//        filter.addFilter(ClassifierFilter(this.includeClassifiers.get(), this.excludeClassifiers.get()))
//        filter.addFilter(GroupIdFilter(this.includeGroupIds.get(), this.excludeGroupIds.get()))
//        filter.addFilter(ArtifactIdFilter(this.includeArtifactIds.get(), this.excludeArtifactIds.get()))
//
//        // explicitly filter our nar dependencies
//        filter.addFilter(TypeFilter("", "nar"))
//
//        // start with all artifacts.
//        val artifactHandler = getProject().getArtifacts()
//        // TODO How to get a set of artifacts from gradle?
//        var artifacts: Set<Artifact>? = null //  artifactHandler;
//
//        // perform filtering
//        try {
//           //  TODO: artifacts = filter.filter(artifacts)
//        } catch (ex: ArtifactFilterException) {
//            throw GradleException(ex.message!!, ex)
//        }
//
//        // transform artifacts if classifier is set
//        val status: DependencyStatusSets
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
//        var artifacts = artifacts
//        val unResolvedArtifacts = HashSet<Artifact>()
//        var resolvedArtifacts = artifacts
//        var status = DependencyStatusSets()
//
//        // possibly translate artifacts into a new set of artifacts based on the
//        // classifier and type
//        // if this did something, we need to resolve the new artifacts
//        if (StringUtils.isNotEmpty(this.copyDepClassifier.get())) {
//            val translator = ClassifierTypeTranslator(
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
//            val artifactsResolver = DefaultArtifactsResolver(this.resolver, null, null, stopOnFailure)
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
//        val filter = FilterArtifacts()
//        filter.clearFilters()
//        val af = DestFileFilter(
//                this.overWriteReleases,
//                this.overWriteSnapshots,
//                this.overWriteIfNewer,
//                false, false, false, false, false, dependenciesDirectory)
//
//        filter.addFilter(af)
//
//        val unMarkedArtifacts: Set<*>
//        try {
//            unMarkedArtifacts = filter.filter(artifacts)
//        } catch (ex: ArtifactFilterException) {
//            throw GradleException(ex.message!!, ex)
//        }
//
//        // calculate the skipped artifacts
//        val skippedArtifacts = HashSet<Artifact>().also {
//            // FIXME: it.addAll(artifacts)
//            it.removeAll(unMarkedArtifacts)
//        }
//
//        return DependencyStatusSets(unMarkedArtifacts as MutableSet<Artifact>?, null, skippedArtifacts)
//    }

    @Throws(GradleException::class)
    protected fun copyFile(artifact: File, destFile: File) {
        try {
            val srcFile = if (this.outputAbsoluteArtifactFilename) artifact.absolutePath else artifact.name
            logger.info("Copying $srcFile to destFile")
            // FileUtils.copyFile(artifact, destFile)
        } catch (ex: Exception) {
            throw GradleException("Error copying artifact from $artifact to $destFile", ex)
        }

    }

    @Throws(GradleException::class)
    private fun makeNar(archive: Jar) {
        val narFile = createArchive(archive)

        // TODO What is the proper way to write the nar?
        if (archiveClassifier != null) {
            //this.getProjectHelper().attachArtifact(getProject(), "nar", this.getArchiveClassifier(), narFile);
        } else {
            //getProject().getArtifacts().setFile(narFile);
        }
    }

    @Throws(GradleException::class)
    fun createArchive(archive: Jar): File {
        val outputDirectory = this.projectBuildDirectory.get().getAsFile()
        val narFile = getNarFile(outputDirectory, this.finalName, archiveClassifier)
//        val archiver = MavenArchiver()
//        archiver.setArchiver(this.jarArchiver)
//        archiver.setOutputFile(narFile)
        // TODO archiver does not have the proper method?
        // archiver.setForced(this.forceCreation.get());

        try {
            val contentDirectory = classesDirectory
            if (contentDirectory.exists()) {
                // TODO problem with Maven archiver.
                // archiver.getArchiver().addDirectory(contentDirectory, getIncludeArray(), getExcludeArray());
            } else {
                logger.warn("NAR will be empty - no content was marked for inclusion!")
            }

            val extensionDocsFile = extensionsDocumentationFile
            if (extensionDocsFile.exists()) {
                // TODO problem with Maven archiver
                // archiver.getArchiver().addFile(extensionDocsFile, "META-INF/docs/" + extensionDocsFile.getName());
            } else {
                logger.warn("NAR will not contain any Extensions' documentation - no META-INF/" + extensionDocsFile.getName() + " file found!")
            }

            val additionalDetailsDirectory = File(extensionsDocumentationFile.getParentFile(), "additional-details")
            if (additionalDetailsDirectory.exists()) {
                // TODO problem with Maven archiver
                // archiver.getArchiver().addDirectory(additionalDetailsDirectory, "META-INF/docs/additional-details/");
            }


            if (this.useDefaultManifestFile) {
                val existingManifest = this.defaultManifestFiles.get().asFile
                if (existingManifest.exists() && archive.manifest == null) {
                    logger.info("Adding existing MANIFEST to archive. Found under: " + existingManifest.path)
                    archive.manifest.from(existingManifest)
                }
            }

            archive.manifest.attributes(  mapOf(
                    "Nar-Id" to this.narId,
                    "Nar-Group" to this.narGroup,
                    "Nar-Version" to this.narVersion
            ) )

            // look for a nar dependency
            val narDependency = this.narDependency
            if (narDependency != null) {
                val narDependencyGroup =
                        if (notEmpty(this.narDependencyGroup.get())) this.narDependencyGroup.get()
                        else narDependency.groupId
                val narDependencyId =
                        if (notEmpty(this.narDependencyId.get())) this.narDependencyId.get()
                        else narDependency.artifactId
                val narDependencyVersion =
                        if (notEmpty(this.narDependencyVersion.get())) this.narDependencyVersion.get()
                        else narDependency.version

                archive.manifest.attributes(  mapOf(
                        "Nar-Dependency-Id" to narDependencyId,
                        "Nar-Dependency-Group" to narDependencyGroup,
                        "Nar-Dependency-Version" to narDependencyVersion
                ) )
            }

            // add build information when available

            if (notEmpty(this.buildTag)) {
                archive.manifest.attributes(mapOf( "Build-Tag" to this.buildTag ), "BUILD")
            }
            if (notEmpty(this.buildBranch)) {
                archive.manifest.attributes(mapOf( "Build-Branch" to this.buildBranch ), "BUILD")
            }
            if (notEmpty(this.buildRevision)) {
                archive.manifest.attributes(mapOf( "Build-Revision" to this.buildRevision ), "BUILD")
            }
            archive.manifest.attributes(mapOf( "Build-Revision" to DATA_FORMAT.format(Date()) ), "BUILD")
            archive.manifest.attributes(mapOf( "Clone-During-Instance-Class-Loading" to this.cloneDuringInstanceClassLoading.toString() ), "BUILD")

            // archive.into( archiveFileName )
            archive.run {  }
            return narFile
        } catch (ex: Exception) {
            throw GradleException("Error assembling NAR", ex)
        }
    }

    private fun notEmpty(value: Property<String>?): Boolean {
        return value != null && !value.get().isEmpty()
    }

    private fun notEmpty(value: String?): Boolean {
        return value != null && !value.isEmpty()
    }


    protected fun getNarFile(basedir: File, finalName: String, in_archiveClassifier: String?): File {
        var archiveClassifier = in_archiveClassifier
        if (archiveClassifier == null) {
            archiveClassifier = ""
        } else if (archiveClassifier.trim { it <= ' ' }.length > 0 && !archiveClassifier.startsWith("-")) {
            archiveClassifier = "-$archiveClassifier"
        }

        return File(basedir, "$finalName$archiveClassifier.nar")
    }

    private class NarDependency(val groupId: String, val artifactId: String, val version: String)


}


