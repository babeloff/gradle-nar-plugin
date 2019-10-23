package org.babeloff.gradle.api.tasks.bundlings


import groovy.lang.Closure
import org.apache.log4j.LogManager
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.file.copy.CopySpecInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact
import org.gradle.internal.impldep.org.apache.maven.plugin.logging.Log
import org.gradle.internal.impldep.org.codehaus.plexus.util.FileUtils
import org.gradle.util.ConfigureUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.JarFile
import javax.xml.stream.XMLOutputFactory

/**
 * Assembles a NAR archive.
 *
 * @author Fred Eisele
 */
open class NarTask : DefaultTask() {

    companion object {
        private val logger = LogManager.getLogger(NarTask::class.java!!)

        val NAR_EXTENSION = "nar"

        val SERVICES_DIRECTORY = "META-INF/services/"
        val BUNDLE_DIRECTORY = "META-INF/bundled-dependencies/"
        val DOCUMENTATION_WRITER_CLASS_NAME = "org.apache.nifi.documentation.xml.XmlDocumentationWriter"

        val DEFAULT_EXCLUDES = arrayOf("**/package.html")
        val DEFAULT_INCLUDES = arrayOf("**/**")

        val BUILD_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        val dateFormat = SimpleDateFormat(BUILD_TIMESTAMP_FORMAT)
    }

    /**
     * Returns the `nifi.xml` file to include in the NAR archive.
     * When `null`, no `nifi.xml` file is included in the NAR.
     *
     * @return The `nifi.xml` file.
     */
    /**
     * Sets the `nifi.xml` file to include in the NAR archive.
     * When `null`, no `nifi.xml` file is included in the NAR.
     *
     * @param nifiXml The `nifi.xml` file. Maybe null.
     */
    @Optional
    @PathSensitive(PathSensitivity.NONE)
    @InputFile
    var nifiXml: RegularFileProperty = project.objects.fileProperty()

    private var classpath: FileCollection? = null


    // BOOLEAN Inputs
    @Input
    val enforceDocGeneration = false
    @Input
    val useDefaultManifestFile = false
    @Input
    val forceCreation = false
    @Input
    var failOnMissingClassifierArtifact = false
    @Input
    val overWriteReleases = false
    @Input
    val overWriteSnapshots = false
    @Input
    val overWriteIfNewer = false
    @Input
    val silent = false
    @Input
    val outputAbsoluteArtifactFilename = false
    @Input
    val cloneDuringInstanceClassLoading = false

    // STRING Inputs

    @Input
    val archiveClassifier = project.objects.property(String::class.java)
    @Input
    val includeTypes = project.objects.property(String::class.java)
    @Input
    val excludeTypes = project.objects.property(String::class.java)
    @Input
    val includeScope = project.objects.property(String::class.java)
    @Input
    val excludeScope = project.objects.property(String::class.java)
    @Input
    val includeClassifiers = project.objects.property(String::class.java)
    @Input
    val excludeClassifiers = project.objects.property(String::class.java)
    @Input
    val copyDepClassifier = project.objects.property(String::class.java)
    @Input
    val type = "nar"
    @Input
    val excludeArtifactIds = project.objects.property(String::class.java)
    @Input
    val includeArtifactIds = project.objects.property(String::class.java)
    @Input
    val excludeGroupIds = project.objects.property(String::class.java)
    @Input
    val includeGroupIds = project.objects.property(String::class.java)
    @Input
    val narDependencyGroup = project.objects.property(String::class.java)
    @Input
    val narDependencyId = project.objects.property(String::class.java)
    @Input
    val narDependencyVersion = project.objects.property(String::class.java)


    @Input
    var metadataCharset = DefaultManifest.DEFAULT_CONTENT_CHARSET
    @Input
    var finalName: String = ""
    @Input
    var narGroup: String = ""
    @Input
    var narId: String = ""
    @Input
    var narVersion: String = ""
    @Input
    var buildTag: String = ""
    @Input
    var buildBranch: String = ""
    @Input
    var buildRevision: String = ""
    @Input
    var archiveExtension: String = ""


    // LIST of STRING Inputs
    @Input
    val includeList = project.objects.listProperty(String::class.java)
    @Input
    val excludeList = project.objects.listProperty(String::class.java)


    // FILESYSTEM Inputs
    @Input
    lateinit var rootSpec: CopySpecInternal
    @Input
    lateinit var mainSpec: CopySpecInternal

    @OutputFile
    val defaultManifestFiles = project.objects.fileProperty()
    @OutputDirectory
    val projectBuildDirectory = project.objects.directoryProperty()

    @OutputDirectory
    val markersDirectory = project.objects.directoryProperty()

    @Input
    val reactorProjects = project.objects.listProperty(String::class.java)


    // QUASI-COMPONENTS Inputs
//    @Input
//    lateinit var archiverManager: ArchiverManager
//
//    @Input
//    lateinit var repositories: ArtifactRepositoryContainer
//
    @Input
    var archive: Jar = Jar()


    // COMPONENTS

//    @Input
//    var jarArchiver: JarArchiver = JarArchiver()
//    @Input
//    var projectHelper: MavenProjectHelper = MavenProjectHelperDefault()
//    @Input
//    var installer: ArtifactInstaller = ArtifactInstallerDefault()
//    @Input
//    var repositoryFactory: ArtifactRepositoryFactory = ArtifactRepositoryFactoryDefault()
//    @Input
//    var factory: ArtifactFactory = ArtifactFactoryDefault()
//    @Input
//    var resolver: ArtifactResolver = ArtifactResolverDefault()
//    @Input
//    var artifactCollector: ArtifactCollector = ActifactCollectorDefault()
//    @Input
//    var artifactMetadataSource: ArtifactMetadataSource = ArtifactMetadataSourceDefault()
//    @Input
//    var artifactHandlerManager: ArtifactHandlerManager = ArtifactHandlerManagerDefault()
//    @Input
//    var dependencyGraphBuilder: DependencyGraphBuilder = DependencyGraphBuilder()
//    @Input
//    var projectBuilder: ProjectBuilder = ProjectBuilderDefault()

    private val metaInf = project.objects.property(CopySpec::class.java!!)


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

    // Construct a maven logger from the actual logger.
    private// this.log = new SystemStreamLog();
    val log: Log?
        get() = if (logger == null) {
            null
        } else null

    private val classesDirectory: File
        get() {
            val outputDirectory = this.projectBuildDirectory.get().getAsFile()
            return File(outputDirectory, "classes")
        }

    private val dependenciesDirectory: File
        get() = File(classesDirectory, "META-INF/bundled-dependencies")

    private// get nar dependencies
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
            val artifactHandler = getProject().getArtifacts()
            var artifacts: Set<Artifact>? = null
//            try {
//                // TODO: artifacts = filter.filter(artifacts).orEmpty()
////            } catch (ex: ArtifactFilterException) {
////                throw GradleException(ex.message!!, ex)
//            }

            if (artifacts!!.size > 1) {
                throw GradleException("Each NAR represents a ClassLoader. A NAR dependency allows that NAR's ClassLoader to be " + "used as the parent of this NAR's ClassLoader. As a result, only a single NAR dependency is allowed.")
            } else if (artifacts!!.size == 1) {
                val artifact = artifacts!!.iterator().next() as Artifact

                narDependency = NarDependency(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion())
            }

            return narDependency
        }

    @Internal(value = "this element provides a copy target for META-INF")
    fun getMetaInf(): CopySpec {
        return this.metaInf.get()
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
        archiveExtension = NAR_EXTENSION
        metadataCharset = DefaultManifest.DEFAULT_CONTENT_CHARSET
        // Add these as separate specs, so they are not affected by the changes to the main spec

        this.metaInf.set(rootSpec
                .addChildBeforeSpec(mainSpec)
                .into("META-INF"))

        // TODO
//        getMetaInf().into("classes", { spec ->
//            spec.from({
//                val classpath = getClasspath()
//                if (classpath != null) classpath!!.filter(Spec<File> { it.isDirectory() }) else emptyList<File>()
//            } as Callable<Iterable<File>>)
//        })
//
//        getMetaInf().into("lib", { spec ->
//            spec.from({
//                val classpath = getClasspath()
//                if (classpath != null) classpath!!.filter(Spec<File> { it.isFile() }) else emptyList<File>()
//            } as Callable<Iterable<File>>)
//        })
//
//        getMetaInf().into("", { spec ->
//            spec.from(Callable<File> { this@NarTask.getNifiXml() } as Callable<File>)
//            spec.rename({ name -> "nifi.xml" })
//        })

        /**
         * Specify which dependencies are to be bundled in the NAR.
         *
         * The bundled-dependencies contains the jar files that will
         * be used by the processor and accompanying controller services
         * (if the NAR contains a controller service).
         * These jar files will be loaded in the ClassLoader that is dedicated to that processor.
         */
        logger!!.trace("configure bundled dependencies()")

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

            if (this.enforceDocGeneration!!) {
                // Catch Throwable in case a linkage error such as NoClassDefFoundError occurs
                logger!!.error("Could not generate extensions' documentation", t)
                throw t
            } else {
                logger!!.warn("Could not generate extensions' documentation", t)
            }
        }

        makeNar()
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
    fun nifiInf(configureClosure: Closure<*>): CopySpec {
        return ConfigureUtil.configure(configureClosure, getMetaInf())
    }

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
    fun nifiInf(configureAction: Action<in CopySpec>): CopySpec {
        val nifiInf = getMetaInf()
        configureAction.execute(nifiInf)
        return nifiInf
    }

    /**
     * Returns the classpath to include in the NAR archive.
     * Any JAR or ZIP files in this classpath are included in the `WEB-INF/lib` directory.
     * Any directories in this classpath are included in the `WEB-INF/classes` directory.
     *
     * @return The classpath. Returns an empty collection when there is no classpath to include in the NAR.
     */
    @Optional
    @Classpath
    fun getClasspath(): FileCollection? {
        return classpath
    }

    /**
     * Sets the classpath to include in the NAR archive.
     *
     * @param classpath The classpath. Must not be null.
     * @since 4.0
     */
    fun setClasspath(classpath: FileCollection) {
        setClasspath(classpath as Any)
    }

    /**
     * Sets the classpath to include in the NAR archive.
     *
     * @param classpath The classpath. Must not be null.
     */
    private fun setClasspath(classpath: Any) {
        this.classpath = getProject().files(classpath)
    }
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
        logger!!.info("Generating documentation for NiFi extensions in the NAR...")

        // Create the ClassLoader for the NAR
//        val classLoaderFactory = createClassLoaderFactory()

//        val extensionClassLoader: ExtensionClassLoader
//        try {
//            extensionClassLoader = classLoaderFactory.createExtensionClassLoader()
//        } catch (e: Exception) {
//            if (this.enforceDocGeneration!!) {
//                throw GradleException("Failed to create Extension Documentation", e)
//            } else {
//                if (logger!!.isDebugEnabled()) {
//                    logger!!.debug("Unable to create a ClassLoader for documenting extensions. If this NAR contains any NiFi Extensions, those extensions will not be documented.", e)
//                } else {
//                    logger!!.warn("Unable to create a ClassLoader for documenting extensions. If this NAR contains any NiFi Extensions, those extensions will not be documented. " + "Enable mvn DEBUG output for more information (mvn -X).")
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
                        logger!!.warn("Cannot locate class $DOCUMENTATION_WRITER_CLASS_NAME, so no documentation will be generated for the extensions in this NAR")
                        return
                    }

//                    logger!!.debug("Creating Extension Definition Factory for NiFi API version $nifiApiVersion")

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
//        logger!!.debug("Generating documentation for " + extensionDefinition.getExtensionName() + " using ClassLoader:\n" + classLoader.toTree())
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

            logger!!.debug("Found file $entryName in $file that consists of documentation for $componentName")
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
//            logger!!.debug(artifact.getFile().getName() + " already exists in destination.")
//        }
    }

//    @Throws(GradleException::class)
//    protected fun copyArtifact(artifact: Artifact) {
//        val destFileName = DependencyUtil.getFormattedFileName(artifact, false)
//        val destDir = DependencyUtil.getFormattedOutputDirectory(false, false, false, false, false, dependenciesDirectory, artifact)
//        val destFile = File(destDir, destFileName)
//        copyFile(artifact.getFile(), destFile)
//    }

    protected fun getResolvedPomArtifact(artifact: Artifact): Artifact {
//        val pomArtifact = this.factory
//                .createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "", "pom")
//        // Resolve the pom artifact using repos
//        try {
//            this.repositories
//            // FIXME: I do not have the maven repositories.
//            // this.getResolver().get().resolve(pomArtifact, this.getRepositories().get(), this.localRepo);
//            this.resolver.resolve(pomArtifact, null, null)
//        } catch (e: ArtifactResolutionException) {
//            logger!!.info(e.message)
//        } catch (e: ArtifactNotFoundException) {
//            logger!!.info(e.message)
//        }
//
//        return pomArtifact
        return artifact
    }

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
            logger!!.info("Copying $srcFile to destFile")
            FileUtils.copyFile(artifact, destFile)
        } catch (ex: Exception) {
            throw GradleException("Error copying artifact from $artifact to $destFile", ex)
        }

    }

    @Throws(GradleException::class)
    private fun makeNar() {
        val narFile = createArchive()

        // TODO What is the proper way to write the nar?
        if (this.archiveClassifier != null) {
            //this.getProjectHelper().attachArtifact(getProject(), "nar", this.getArchiveClassifier(), narFile);
        } else {
            //getProject().getArtifacts().setFile(narFile);
        }
    }

    @Throws(GradleException::class)
    fun createArchive(): File {
        val outputDirectory = this.projectBuildDirectory.get().getAsFile()
        val narFile = getNarFile(outputDirectory, this.finalName, this.archiveClassifier!!.get())
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
                logger!!.warn("NAR will be empty - no content was marked for inclusion!")
            }

            val extensionDocsFile = extensionsDocumentationFile
            if (extensionDocsFile.exists()) {
                // TODO problem with Maven archiver
                // archiver.getArchiver().addFile(extensionDocsFile, "META-INF/docs/" + extensionDocsFile.getName());
            } else {
                logger!!.warn("NAR will not contain any Extensions' documentation - no META-INF/" + extensionDocsFile.getName() + " file found!")
            }

            val additionalDetailsDirectory = File(extensionsDocumentationFile.getParentFile(), "additional-details")
            if (additionalDetailsDirectory.exists()) {
                // TODO problem with Maven archiver
                // archiver.getArchiver().addDirectory(additionalDetailsDirectory, "META-INF/docs/additional-details/");
            }


            if (this.useDefaultManifestFile!!) {
                val existingManifest = this.defaultManifestFiles.get().asFile
                if (existingManifest.exists() && this.archive.manifest == null) {
                    logger!!.info("Adding existing MANIFEST to archive. Found under: " + existingManifest.path)
                    this.archive.manifest.from(existingManifest)
                }
            }

            // automatically add the artifact id, group id, and version to the manifest
            val narAttributesMap =

            this.archive.manifest.attributes(  mapOf(
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

                this.archive.manifest.attributes(  mapOf(
                        "Nar-Dependency-Id" to narDependencyId,
                        "Nar-Dependency-Group" to narDependencyGroup,
                        "Nar-Dependency-Version" to narDependencyVersion
                ) )
            }

            // add build information when available

            if (notEmpty(this.buildTag)) {
                this.archive.manifest.attributes(mapOf( "Build-Tag" to this.buildTag ), "BUILD")
            }
            if (notEmpty(this.buildBranch)) {
                this.archive.manifest.attributes(mapOf( "Build-Branch" to this.buildBranch ), "BUILD")
            }
            if (notEmpty(this.buildRevision)) {
                this.archive.manifest.attributes(mapOf( "Build-Revision" to this.buildRevision ), "BUILD")
            }
            this.archive.manifest.attributes(mapOf( "Build-Revision" to dateFormat.format(Date()) ), "BUILD")
            this.archive.manifest.attributes(mapOf( "Clone-During-Instance-Class-Loading" to this.cloneDuringInstanceClassLoading.toString() ), "BUILD")

            this.archive.into( this.archiveFile)
            // archiver.createArchive(getProject(), archive);
            return narFile
        } catch (ex: Exception) {
            throw GradleException("Error assembling NAR", ex)
        }
    }

    private fun notEmpty(value: Property<String>?): Boolean {
        return value != null && !value!!.get().isEmpty()
    }

    private fun notEmpty(value: String?): Boolean {
        return value != null && !value.isEmpty()
    }


    protected fun getNarFile(basedir: File, finalName: String, archiveClassifier: String?): File {
        var archiveClassifier = archiveClassifier
        if (archiveClassifier == null) {
            archiveClassifier = ""
        } else if (archiveClassifier.trim { it <= ' ' }.length > 0 && !archiveClassifier.startsWith("-")) {
            archiveClassifier = "-$archiveClassifier"
        }

        return File(basedir, "$finalName$archiveClassifier.nar")
    }

    private class NarDependency(val groupId: String, val artifactId: String, val version: String)


}


