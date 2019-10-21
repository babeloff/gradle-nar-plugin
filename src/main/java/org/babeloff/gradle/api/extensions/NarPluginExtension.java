package org.babeloff.gradle.api.extensions;
/**
 * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPluginConvention.java
 */

import org.babeloff.gradle.api.annotations.Component;
import org.babeloff.gradle.api.annotations.Parameter;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.DependencyGraphBuilder;
import org.gradle.api.plugins.Convention;

import java.io.File;
import java.util.List;

import org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar.JarArchiver;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.gradle.internal.impldep.org.apache.maven.archiver.MavenArchiveConfiguration;
import org.gradle.internal.impldep.org.apache.maven.artifact.factory.ArtifactFactory;
import org.gradle.internal.impldep.org.apache.maven.artifact.handler.ArtifactHandler;
import org.gradle.internal.impldep.org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.gradle.internal.impldep.org.apache.maven.artifact.installer.ArtifactInstaller;
import org.gradle.internal.impldep.org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactResolver;
import org.gradle.internal.impldep.org.apache.maven.project.MavenProject;
import org.gradle.internal.impldep.org.apache.maven.project.MavenProjectHelper;
import org.gradle.internal.impldep.org.apache.maven.project.ProjectBuilder;

/**
 * <p>A {@link Convention} used for the NarPlugin.</p>
 * The specification for which can be found at:
 * https://gitbox.apache.org/repos/asf?p=nifi-maven.git
 * https://gitbox.apache.org/repos/asf?p=nifi-maven.git;a=blob;f=src/main/java/org/apache/nifi/NarMojo.java
 *
 */
abstract public class NarPluginExtension
{

    // TODO: Do we need the components?

    /**
     * POM
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    abstract public Project getProject();
    abstract public void setProject(Project value);

    //@Parameter(defaultValue = "${session}", readonly = true, required = true)
    //public MavenSession session;

    /**
     * List of files to include. Specified as fileset patterns.
     */
    @Parameter(property = "includes")
    abstract public String[] getIncludes();
    abstract public void setIncludes(String[] value);
    
    /**
     * List of files to exclude. Specified as fileset patterns.
     */
    @Parameter(property = "excludes")
    abstract public String[] getExcludes();
    abstract public void setExcludes(String[] value);
    /**
     * Name of the generated NAR.
     *
     */
    @Parameter(alias = "narName", property = "nar.finalName",
            defaultValue = "${project.getName()}", required = true)
    abstract public String getFinalName();
    abstract public void setFinalName(String value);

    /**
     * The Jar archiver.
     *
     * \@\component role="org.codehaus.plexus.archiver.Archiver" roleHint="jar"
     */
    @Component(role = org.gradle.internal.impldep.org.codehaus.plexus.archiver.Archiver.class, hint = "jar")
    abstract protected JarArchiver getJarArchiver();
    abstract protected void setJarArchiver(JarArchiver value);

    /**
     * The archive configuration to use.
     *
     * See <a
     * href="http://maven.apache.org/shared/maven-archiver/index.html">the
     * documentation for Maven Archiver</a>.
     *
     */
    @Parameter(property = "archive")
    abstract public MavenArchiveConfiguration getArchive();

    /**
     * Path to the default MANIFEST file to use. It will be used if
     * <code>useDefaultManifestFile</code> is set to <code>true</code>.
     *
     */
    @Parameter(property = "defaultManifestFiles",
            defaultValue = "${project.build.outputDirectory}/META-INF/MANIFEST.MF",
            readonly = true,
            required = true)
    abstract public File getDefaultManifestFiles();
    abstract public void setDefaultManifestFiles(File value);

    /**
     * Set this to <code>true</code> to enable the use of the
     * <code>defaultManifestFile</code>.
     *
     * @since 2.2
     */
    @Parameter(property = "nar.useDefaultManifestFile", defaultValue = "false")
    abstract public boolean getUseDefaultManifestFile();
    abstract public void setUseDefaultManifestFile(boolean value);

    @Component
    abstract public MavenProjectHelper getProjectHelper();

    /**
     * Whether creating the archive should be forced.
     *
     */
    @Parameter(property = "nar.forceCreation", defaultValue = "false")
    abstract public boolean getForceCreation();
    abstract public void setForceCreation(boolean value);

    /**
     * Classifier to add to the artifact generated.
     * If given, the artifact will be an attachment instead.
     */
    @Parameter(property = "classifier")
    abstract public String getClassifier();
    abstract public void setClassifier(String value);

    @Component
    abstract public ArtifactInstaller getInstaller();

    @Component
    abstract public ArtifactRepositoryFactory getRepositoryFactory();

    /**
     * This only applies if the classifier parameter is used.
     *
     */
    @Parameter(property = "mdep.failOnMissingClassifierArtifact", defaultValue = "true", required = false)
    abstract public boolean getFailOnMissingClassifierArtifact();
    abstract public void setFailOnMissingClassifierArtifact(boolean value);

    /**
     * Comma Separated list of Types to include. Empty String indicates include
     * everything (default).
     *
     */
    @Parameter(property = "includeTypes", required = false)
    abstract public String getIncludeTypes();
    abstract public void setIncludeTypes(String value);

    /**
     * Comma Separated list of Types to exclude. Empty String indicates don't
     * exclude anything (default).
     *
     */
    @Parameter(property = "excludeTypes", required = false)
    abstract public String getExcludeTypes();
    abstract public void setExcludeTypes(String value);

    /**
     * Scope to include. An Empty string indicates all scopes (default).
     *
     */
    @Parameter(property = "includeScope", required = false)
    abstract public String getIncludeScope();
    abstract public void setIncludeScope(String value);

    /**
     * Scope to exclude. An Empty string indicates no scopes (default).
     *
     */
    @Parameter(property = "excludeScope", required = false)
    abstract public String getExcludeScope();
    abstract public void setExcludeScope(String value);

    /**
     * Comma Separated list of Classifiers to include. Empty String indicates
     * include everything (default).
     *
     */
    @Parameter(property = "includeClassifiers", required = false)
    abstract public String getIncludeClassifiers();
    abstract public void setIncludeClassifiers(String value);

    /**
     * Comma Separated list of Classifiers to exclude. Empty String indicates
     * don't exclude anything (default).
     *
     */
    @Parameter(property = "excludeClassifiers", required = false)
    abstract public String getExcludeClassifiers();
    abstract public void setExcludeClassifiers(String value);

    /**
     * Specify classifier to look for. Example: sources
     *
     */
    @Parameter(property = "classifier", required = false)
    abstract public String getCopyDepClassifier();
    abstract public void setCopyDepClassifier(String value);

    /**
     * Specify type to look for when constructing artifact based on classifier.
     * Example: java-source,jar,war, nar
     *
     */
    @Parameter(property = "type", required = true, defaultValue = "nar")
    abstract public String getType();
    abstract public void setType(String value);

    /**
     * Comma separated list of Artifact names too exclude.
     *
     */
    @Parameter(property = "excludeArtifacts", required = false)
    abstract public String getExcludeArtifactIds();
    abstract public void setExcludeArtifactIds(String value);

    /**
     * Comma separated list of Artifact names to include.
     *
     */
    @Parameter(property = "includeArtifacts", required = false)
    abstract public String getIncludeArtifactIds();
    abstract public void setIncludeArtifactIds(String value);

    /**
     * Comma separated list of GroupId Names to exclude.
     *
     */
    @Parameter(property = "excludeArtifacts", required = false)
    abstract public String getExcludeGroupIds();
    abstract public void setExcludeGroupIds(String value);

    /**
     * Comma separated list of GroupIds to include.
     *
     */
    @Parameter(property = "includeGroupIds", required = false)
    abstract public String getIncludeGroupIds();
    abstract public void setIncludeGroupIds(String value);

    /**
     * Directory to store flag files
     *
     */
    @Parameter(property = "markersDirectory", required = true,
            defaultValue = "${project.build.directory}/dependency-maven-plugin-markers")
    abstract public Directory getMarkersDirectory();
    abstract public void setMarkersDirectory(Directory value);

    /**
     * Overwrite release artifacts
     *
     */
    @Parameter(property = "overWriteReleases", required = true)
    abstract public boolean getOverWriteReleases();
    abstract public void setOverWriteReleases(boolean value);

    /**
     * Overwrite snapshot artifacts
     *
     */
    @Parameter(property = "overWriteSnapshots", required = true)
    abstract public boolean getOverWriteSnapshots();
    abstract public void setOverWriteSnapshots(boolean value);

    /**
     * Overwrite artifacts that don't exist or are older than the source.
     *
     */
    @Parameter(property = "overWriteIfNewer", required = true, defaultValue = "true")
    abstract public boolean getOverWriteIfNewer();
    abstract public void setOverWriteIfNewer(boolean value);

    @Parameter(property = "projectBuildDirectory", 
            required = true,
            defaultValue = "${project.build.directory}")
    abstract public Directory getProjectBuildDirectory();
    abstract public void setProjectBuildDirectory(Directory value);

    /**
     * Used to look up Artifacts in the remote repository.
     */
    @Component
    abstract public ArtifactFactory getFactory();

    /**
     * Used to look up Artifacts in the remote repository.
     *
     */
    @Component
    abstract public ArtifactResolver getResolver();

    /**
     * Container of Repositories used by the resolver
     *
     */
    @Parameter(property = "project.remoteArtifactRepositories", required = true, readonly = true)
    abstract public ArtifactRepositoryContainer getRepositories();
    abstract public void setRepositories(ArtifactRepositoryContainer value);

    /**
     * To look up Archiver/UnArchiver implementations
     *
     */
    @Component
    abstract public ArchiverManager getArchiverManager();
    abstract public void setArchiverManager(ArchiverManager value);

    /**
     * Contains the full list of projects in the reactor.
     *
     */
    @Parameter(property = "reactorProjects", required = true, readonly = true)
    abstract public List getReactorProjects();
    abstract public void setReactorProjects(List value);

    /**
     * If the plugin should be silent.
     *
     */
    @Parameter(property = "silent", required = false, defaultValue = "false")
    abstract public boolean getSilent();
    abstract public void setSilent(boolean value);

    /**
     * The dependency tree builder to use for verbose output.
     */
    @Component
    abstract public DependencyGraphBuilder getDependencyGraphBuilder();

    /**
     * *
     * The {@link ArtifactHandlerManager} into which any extension {@link ArtifactHandler} instances should have been injected when the extensions were loaded.
     */
    @Component
    abstract public ArtifactHandlerManager getArtifactHandlerManager();

    /**
     * Output absolute filename for resolved artifacts
     *
     */
    @Parameter(property = "outputAbsoluteArtifactFilename", defaultValue = "false", required = false)
    abstract public boolean getOutputAbsoluteArtifactFilename();
    abstract public void setOutputAbsoluteArtifactFilename(boolean value);

    /**
     * The values to use for populating the Nar-Group, Nar-Id, and Nar-Version in the MANIFEST file. 
     * By default these values will be set to the standard Maven project equivalents, 
     * but they may be overridden through properties.
     *
     * For example if the build.gradle.kts for the nifi-test-nar contained the following:
     *
     *    groupId = "org.apache.nifi"
     *    artifactId = nifi-test-nar"
     *    version = "1.0"
     *
     *    nar {
     *       narGroup = "org.apache.nifi.overridden"
     *       narId = "nifi-overridden-test-nar"
     *       narVersion = "2.0"
     *   }
     *
     * It would produce a MANIFEST with:
     *
     *   Nar-Id: nifi-overridden-test-nar
     *   Nar-Group: org.apache.nifi.overridden
     *   Nar-Version: 2.0
     *
     */

    @Parameter(property = "narGroup", defaultValue = "${project.groupId}", required = true)
    abstract public String getNarGroup();
    abstract public void setNarGroup(String value);

    @Parameter(property = "narId", defaultValue = "${project.artifactId}", required = true)
    abstract public String getNarId();
    abstract public void setNarId(String value);

    @Parameter(property = "narVersion", defaultValue = "${project.version}", required = true)
    abstract public String getNarVersion();
    abstract public void setNarVersion(String value);

    @Parameter(property = "narDependencyGroup", required = false, defaultValue="null")
    abstract public String getNarDependencyGroup();
    abstract public void setNarDependencyGroup(String value);

    @Parameter(property = "narDependencyId", required = false, defaultValue="null")
    abstract public String getNarDependencyId();
    abstract public void setNarDependencyId(String value);

    @Parameter(property = "narDependencyVersion", required = false, defaultValue="null")
    abstract public String getNarDependencyVersion();
    abstract public void setNarDependencyVersion(String value);


    /**
     * Build info to be populated in MANIFEST.
     */

    @Parameter(property = "buildTag", defaultValue = "${project.scm.tag}", required = false)
    abstract public String getBuildTag();
    abstract public void setBuildTag(String value);

    @Parameter(property = "buildBranch", defaultValue = "${buildBranch}", required = false)
    abstract public String getBuildBranch();
    abstract public void setBuildBranch(String value);

    @Parameter(property = "buildRevision", defaultValue = "${buildRevision}", required = false)
    abstract public String getBuildRevision();
    abstract public void setBuildRevision(String value);

    /**
     * Allows a NAR to specify if it's resources should be cloned when a component that depends on this NAR
     * is performing class loader isolation.
     */
    @Parameter(property = "cloneDuringInstanceClassLoading", defaultValue = "false", required = false)
    abstract public boolean getCloneDuringInstanceClassLoading();
    abstract public void setCloneDuringInstanceClassLoading(boolean value);


    @Parameter(property = "enforceDocGeneration", defaultValue = "false", required = false)
    abstract public boolean getEnforceDocGeneration();
    abstract public void setEnforceDocGeneration(boolean value);

    /**
     * The {@link RepositorySystemSession} used for obtaining the local and remote artifact repositories.
     */
    //@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    //private RepositorySystemSession getRepoSession();


    /**
     * The {@link ProjectBuilder} used to generate the {@link MavenProject}
     * for the nar artifact the dependency tree is being generated for.
     */
    @Component
    abstract public ProjectBuilder getProjectBuilder();


}
