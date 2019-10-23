package org.babeloff.gradle.api.extensions

/**
 * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPluginConvention.java
 */

import org.babeloff.gradle.api.annotations.Component
import org.babeloff.gradle.api.annotations.Parameter
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.api.file.Directory
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.DependencyGraphBuilder
import org.gradle.internal.impldep.org.apache.maven.artifact.handler.ArtifactHandler
import org.gradle.internal.impldep.org.apache.maven.artifact.handler.manager.ArtifactHandlerManager
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 *
 * Uused for the NarPlugin.
 * The specification for which can be found at:
 * https://gitbox.apache.org/repos/asf?p=nifi-maven.git
 * https://gitbox.apache.org/repos/asf?p=nifi-maven.git;a=blob;f=src/main/java/org/apache/nifi/NarMojo.java
 *
 */
open class NarPluginExtension(project: Project) {
    // helpers
    private val versionFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

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
    val includeList = project.objects.listProperty(String::class.java)

    /**
     * List of files to exclude. Specified as fileset patterns.
     */
    @Parameter(property = "excludes")
    val excludeList = project.objects.listProperty(String::class.java)
    /**
     * Name of the generated NAR.
     *
     */
    @Parameter(alias = "narName", property = "nar.finalName",
            defaultValue = "\${project.getName()}", required = true)
    var finalName: String = project.name

    /**
     * The Jar archiver.
     *
     * \@\component role="org.codehaus.plexus.archiver.Archiver" roleHint="jar"
     */
//    @Component(role = org.gradle.internal.impldep.org.codehaus.plexus.archiver.Archiver::class, hint = "jar")
//    lateinit protected var jarArchiver: JarArchiver

    /**
     * The archive configuration to use.
     *
     * See [the
 * documentation for Maven Archiver](http://maven.apache.org/shared/maven-archiver/index.html).
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
    var defaultManifestFiles: File = File(project.buildDir.canonicalFile,  "META-INF/MANIFEST.MF")

    /**
     * Set this to `true` to enable the use of the
     * `defaultManifestFile`.
     *
     * @since 2.2
     */
    @Parameter(property = "nar.useDefaultManifestFile", defaultValue = "false")
    var useDefaultManifestFile = false


//    @Component
//    val projectHelper: MavenProjectHelper = MavenProjectHelperDefault()

    /**
     * Whether creating the archive should be forced.
     *
     */
    @Parameter(property = "nar.forceCreation", defaultValue = "false")
    var forceCreation = false

    /**
     * Classifier to add to the artifact generated.
     * If given, the artifact will be an attachment instead.
     */
    @Parameter(property = "classifier")
    var classifier = "nar"


//    @Component
//    val installer: ArtifactInstaller =  ArtifactInstallerDefault()


//    @Component
//    val repositoryFactory: ArtifactRepositoryFactory = ArtifactRepositoryFactoryDefault()

    /**
     * This only applies if the classifier parameter is used.
     *
     */
    @Parameter(property = "mdep.failOnMissingClassifierArtifact", defaultValue = "true", required = false)
    var failOnMissingClassifierArtifact = true

    /**
     * Comma Separated list of Types to include. Empty String indicates include
     * everything (default).
     *
     */
    @Parameter(property = "includeTypes", required = false)
    val includeTypes = project.objects.property(String::class.java)

    /**
     * Comma Separated list of Types to exclude. Empty String indicates don't
     * exclude anything (default).
     *
     */
    @Parameter(property = "excludeTypes", required = false)
    val excludeTypes = project.objects.property(String::class.java)

    /**
     * Scope to include. An Empty string indicates all scopes (default).
     *
     */
    @Parameter(property = "includeScope", required = false)
    val includeScope = project.objects.property(String::class.java)

    /**
     * Scope to exclude. An Empty string indicates no scopes (default).
     *
     */
    @Parameter(property = "excludeScope", required = false)
    val excludeScope = project.objects.property(String::class.java)

    /**
     * Comma Separated list of Classifiers to include. Empty String indicates
     * include everything (default).
     *
     */
    @Parameter(property = "includeClassifiers", required = false)
    val includeClassifiers = project.objects.property(String::class.java)

    /**
     * Comma Separated list of Classifiers to exclude. Empty String indicates
     * don't exclude anything (default).
     *
     */
    @Parameter(property = "excludeClassifiers", required = false)
    val excludeClassifiers = project.objects.property(String::class.java)

    /**
     * Specify classifier to look for. Example: sources
     *
     */
    @Parameter(property = "classifier", required = false)
    val copyDepClassifier = project.objects.property(String::class.java)

    /**
     * Specify type to look for when constructing artifact based on classifier.
     * Example: java-source,jar,war, nar
     *
     */
    @Parameter(property = "type", required = true, defaultValue = "nar")
    var type = "nar"

    /**
     * Comma separated list of Artifact names too exclude.
     *
     */
    @Parameter(property = "excludeArtifacts", required = false)
    val excludeArtifactIds = project.objects.property(String::class.java)

    /**
     * Comma separated list of Artifact names to include.
     *
     */
    @Parameter(property = "includeArtifacts", required = false)
    val includeArtifactIds = project.objects.property(String::class.java)

    /**
     * Comma separated list of GroupId Names to exclude.
     *
     */
    @Parameter(property = "excludeArtifacts", required = false)
    val excludeGroupIds = project.objects.property(String::class.java)

    /**
     * Comma separated list of GroupIds to include.
     *
     */
    @Parameter(property = "includeGroupIds", required = false)
    val includeGroupIds = project.objects.property(String::class.java)

    /**
     * Directory to store flag files
     *
     */
    @Parameter(property = "markersDirectory", required = true,
            defaultValue = "\${project.build.directory}/dependency-maven-plugin-markers")
    val markersDirectory: Directory

    /**
     * Overwrite release artifacts
     *
     */
    @Parameter(property = "overWriteReleases", required = true)
    var overWriteReleases = false

    /**
     * Overwrite snapshot artifacts
     *
     */
    @Parameter(property = "overWriteSnapshots", required = true)
    var overWriteSnapshots = false

    /**
     * Overwrite artifacts that don't exist or are older than the source.
     *
     */
    @Parameter(property = "overWriteIfNewer", required = true, defaultValue = "true")
    var overWriteIfNewer = true

    @Parameter(property = "projectBuildDirectory", required = true, defaultValue = "\${project.build.directory}")
    val projectBuildDirectory = project.objects.directoryProperty()

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
    var repositories: ArtifactRepositoryContainer

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
    val reactorProjects = project.objects.listProperty(String::class.java)

    /**
     * If the plugin should be silent.
     *
     */
    @Parameter(property = "silent", required = false, defaultValue = "false")
    var silent = false

    /**
     * The dependency tree builder to use for verbose output.
     */
    @Component
    val dependencyGraphBuilder: DependencyGraphBuilder = DependencyGraphBuilder(
            null, // DependencyToComponentIdResolver componentIdResolver,
            null, // ComponentMetaDataResolver componentMetaDataResolver,
            null, // ResolveContextToComponentResolver resolveContextToComponentResolver,
            null, // ModuleConflictHandler moduleConflictHandler,
            null, // CapabilitiesConflictHandler capabilitiesConflictHandler,
            null, // Spec<? super DependencyMetadata> edgeFilter,
            null, // AttributesSchemaInternal attributesSchema,
            null, // ModuleExclusions moduleExclusions,
            null, // BuildOperationExecutor buildOperationExecutor,
            null, // ModuleReplacementsData moduleReplacementsData,
            null, // DependencySubstitutionApplicator dependencySubstitutionApplicator,
            null, // ComponentSelectorConverter componentSelectorConverter,
            null, // ImmutableAttributesFactory attributesFactory,
            null, // VersionSelectorScheme versionSelectorScheme,
            null, // Comparator<Version> versionComparator,
            null // VersionParser versionParser
            )

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
    var narGroup: String

    @Parameter(property = "narId", defaultValue = "\${project.artifactId}", required = true)
    var narId: String

    @Parameter(property = "narVersion", defaultValue = "\${project.version}", required = true)
    var narVersion: String

    @Parameter(property = "narDependencyGroup", required = false, defaultValue = "null")
    val narDependencyGroup = project.objects.property(String::class.java)

    @Parameter(property = "narDependencyId", required = false, defaultValue = "null")
    val narDependencyId = project.objects.property(String::class.java)

    @Parameter(property = "narDependencyVersion", required = false, defaultValue = "null")
    val narDependencyVersion = project.objects.property(String::class.java)


    /**
     * Build info to be populated in MANIFEST.
     */

    @Parameter(property = "buildTag", defaultValue = "\${project.scm.tag}", required = false)
    var buildTag: String

    @Parameter(property = "buildBranch", defaultValue = "\${buildBranch}", required = false)
    var buildBranch: String

    @Parameter(property = "buildRevision", defaultValue = "\${buildRevision}", required = false)
    var buildRevision: String

    /**
     * Allows a NAR to specify if it's resources should be cloned when a component that depends on this NAR
     * is performing class loader isolation.
     */
    @Parameter(property = "cloneDuringInstanceClassLoading", defaultValue = "false", required = false)
    var cloneDuringInstanceClassLoading = false


    @Parameter(property = "enforceDocGeneration", defaultValue = "false", required = false)
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

    init {
        val layout = project.getLayout()
        val projectDir = layout.getProjectDirectory()

        this.finalName = project.getName()
        this.defaultManifestFiles = File(project.getBuildDir(), "META-INF/MANIFEST.MF")
        this.markersDirectory = projectDir.dir(File(project.getBuildDir(), "dependency-maven-plugin-markers").getPath())
        this.projectBuildDirectory.set(projectDir.dir(project.getBuildDir().getAbsolutePath()))
        this.repositories = project.getRepositories()

        this.narGroup = project.getGroup().toString()
        this.narId = project.getName()
        this.narVersion = project.getVersion().toString()

        // https://github.com/nebula-plugins/gradle-info-plugin#info-scm-plugin-collector
        this.buildTag = ""
        this.buildBranch = "develop"
        this.buildRevision = versionFormatter.format(LocalDateTime.now())
    }


}
