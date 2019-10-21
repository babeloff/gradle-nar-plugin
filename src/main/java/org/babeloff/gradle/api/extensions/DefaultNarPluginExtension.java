package org.babeloff.gradle.api.extensions;

import org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar.JarArchiver;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.DependencyGraphBuilder;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.impldep.org.apache.maven.archiver.MavenArchiveConfiguration;
import org.gradle.internal.impldep.org.apache.maven.artifact.factory.ArtifactFactory;
import org.gradle.internal.impldep.org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.gradle.internal.impldep.org.apache.maven.artifact.installer.ArtifactInstaller;
import org.gradle.internal.impldep.org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactCollector;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactResolver;
import org.gradle.internal.impldep.org.apache.maven.project.MavenProjectHelper;
import org.gradle.internal.impldep.org.apache.maven.project.ProjectBuilder;
import org.gradle.internal.impldep.org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.manager.DefaultArchiverManager;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.gradle.api.reflect.TypeOf.typeOf;

public class DefaultNarPluginExtension extends NarPluginExtension implements HasPublicType {

    // helpers
    private DateTimeFormatter versionFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    // Parameters
    private boolean enforceDocGeneration = false;
    private boolean useDefaultManifestFile = false;
    private boolean forceCreation = false;
    private boolean failOnMissingClassifierArtifact = true;
    private boolean overWriteReleases = false;
    private boolean overWriteSnapshots = false;
    private boolean overWriteIfNewer = true;
    private boolean silent = false;
    private boolean outputAbsoluteArtifactFilename = false;
    private boolean cloneDuringInstanceClassLoading = false;

    private String classifier = null;
    private String includeTypes = null;
    private String excludeTypes = null;
    private String includeScope = null;
    private String excludeScope = null;
    private String includeClassifiers = null;
    private String excludeClassifiers = null;
    private String copyDepClassifier = null;
    private String type = "nar";
    private String excludeArtifactIds = null;
    private String includeArtifactIds = null;
    private String excludeGroupIds = null;
    private String includeGroupIds = null;
    private String narDependencyGroup = null;
    private String narDependencyId = null;
    private String narDependencyVersion = null;

    private String finalName;
    private String narGroup;
    private String narId;
    private String narVersion;
    private String buildTag;
    private String buildBranch;
    private String buildRevision;

    private String[] excludes;
    private String[] includes;


    private File defaultManifestFiles;
    private Directory projectBuildDirectory ;
    private Directory markersDirectory;

    private List reactorProjects = new ArrayList(0);

    private ArchiverManager archiverManager = new DefaultArchiverManager();

    private Project project;
    private ArtifactRepositoryContainer repositories;
    private final MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    // components
    private JarArchiver jarArchiver;
    private MavenProjectHelper projectHelper;
    private ArtifactInstaller installer;
    private ArtifactRepositoryFactory repositoryFactory;
    private ArtifactFactory factory;
    private ArtifactResolver resolver;
    private ArtifactCollector artifactCollector;
    private ArtifactMetadataSource artifactMetadataSource;
    private ArtifactHandlerManager artifactHandlerManager;
    private DependencyGraphBuilder dependencyGraphBuilder;
    private ProjectBuilder projectBuilder;


    public DefaultNarPluginExtension(Project project) {
        final ProjectLayout layout = project.getLayout();
        final Directory projectDir = layout.getProjectDirectory();
        this.project = project;
        this.finalName = project.getName();
        this.defaultManifestFiles = new File(project.getBuildDir(), "META-INF/MANIFEST.MF");
        this.markersDirectory = projectDir.dir(new File(project.getBuildDir(), "dependency-maven-plugin-markers").getPath());
        this.projectBuildDirectory = projectDir.dir(project.getBuildDir().getAbsolutePath());
        this.repositories = project.getRepositories();

        this.narGroup = project.getGroup().toString();
        this.narId = project.getName();
        this.narVersion = project.getVersion().toString();

        // https://github.com/nebula-plugins/gradle-info-plugin#info-scm-plugin-collector
        this.buildTag = "";
        this.buildBranch = "develop";
        this.buildRevision = versionFormatter.format(LocalDateTime.now());
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(NarPluginExtension.class);
    }

    @Override
    public Project getProject() {
        return this.project;
    }

    @Override
    public void setProject(Project value)
    {
        this.project = value;
    }

    @Override
    public String[] getIncludes()
    {
        return this.includes;
    }

    @Override
    public void setIncludes(String[] value)
    {
        this.includes = value;
    }

    @Override
    public String[] getExcludes()
    {
        return this.excludes;
    }

    @Override
    public void setExcludes(String[] value)
    {
        this.excludes = value;
    }

    @Override
    public String getFinalName()
    {
        return this.finalName;
    }

    @Override
    public void setFinalName(String value)
    {
        this.finalName = value;
    }

    @Override
    protected JarArchiver getJarArchiver()
    {
        return this.jarArchiver;
    }

    @Override
    protected void setJarArchiver(JarArchiver value)
    {
        this.jarArchiver = value;
    }

    @Override
    public MavenArchiveConfiguration getArchive()
    {
        return this.archive;
    }

    @Override
    public File getDefaultManifestFiles()
    {
        return this.defaultManifestFiles;
    }

    @Override
    public void setDefaultManifestFiles(File value)
    {
        this.defaultManifestFiles = value;
    }

    @Override
    public boolean getUseDefaultManifestFile()
    {
        return this.useDefaultManifestFile;
    }

    @Override
    public void setUseDefaultManifestFile(boolean value)
    {
        this.useDefaultManifestFile = value;
    }

    @Override
    public MavenProjectHelper getProjectHelper()
    {
        return this.projectHelper;
    }

    @Override
    public boolean getForceCreation()
    {
        return this.forceCreation;
    }

    @Override
    public void setForceCreation(boolean value)
    {
        this.forceCreation = value;
    }

    @Override
    public String getClassifier()
    {
        return this.classifier;
    }

    @Override
    public void setClassifier(String value)
    {
        this.classifier = value;
    }

    @Override
    public ArtifactInstaller getInstaller()
    {
        return this.installer;
    }

    @Override
    public ArtifactRepositoryFactory getRepositoryFactory()
    {
        return this.repositoryFactory;
    }

    @Override
    public boolean getFailOnMissingClassifierArtifact()
    {
        return this.failOnMissingClassifierArtifact;
    }

    @Override
    public void setFailOnMissingClassifierArtifact(boolean value)
    {
        this.failOnMissingClassifierArtifact = value;
    }

    @Override
    public String getIncludeTypes()
    {
        return this.includeTypes;
    }

    @Override
    public void setIncludeTypes(String value)
    {
        this.includeTypes = value;
    }

    @Override
    public String getExcludeTypes()
    {
        return this.excludeTypes;
    }

    @Override
    public void setExcludeTypes(String value)
    {
        this.excludeTypes = value;
    }

    @Override
    public String getIncludeScope()
    {
        return this.includeScope;
    }

    @Override
    public void setIncludeScope(String value)
    {
        this.includeScope = value;
    }

    @Override
    public String getExcludeScope()
    {
        return this.excludeScope;
    }

    @Override
    public void setExcludeScope(String value)
    {
        this.excludeScope = value;
    }

    @Override
    public String getIncludeClassifiers()
    {
        return this.includeClassifiers;
    }

    @Override
    public void setIncludeClassifiers(String value)
    {
        this.includeClassifiers = value;
    }

    @Override
    public String getExcludeClassifiers()
    {
        return this.excludeClassifiers;
    }

    @Override
    public void setExcludeClassifiers(String value)
    {
        this.excludeClassifiers = value;
    }

    @Override
    public String getCopyDepClassifier()
    {
        return this.copyDepClassifier;
    }

    @Override
    public void setCopyDepClassifier(String value)
    {
        this.copyDepClassifier = value;
    }

    @Override
    public String getType()
    {
        return this.type;
    }

    @Override
    public void setType(String value)
    {
        this.type = value;
    }

    @Override
    public String getExcludeArtifactIds()
    {
        return this.excludeArtifactIds;
    }

    @Override
    public void setExcludeArtifactIds(String value)
    {
        this.excludeArtifactIds = value;
    }

    @Override
    public String getIncludeArtifactIds()
    {
        return this.includeArtifactIds;
    }

    @Override
    public void setIncludeArtifactIds(String value)
    {
        this.includeArtifactIds = value;
    }

    @Override
    public String getExcludeGroupIds()
    {
        return this.excludeGroupIds;
    }

    @Override
    public void setExcludeGroupIds(String value)
    {
        this.excludeGroupIds = value;
    }

    @Override
    public String getIncludeGroupIds()
    {
        return this.includeGroupIds;
    }

    @Override
    public void setIncludeGroupIds(String value)
    {
        this.includeGroupIds = value;
    }

    @Override
    public Directory getMarkersDirectory()
    {
        return this.markersDirectory;
    }

    @Override
    public void setMarkersDirectory(Directory value)
    {
        this.markersDirectory = value;
    }

    @Override
    public boolean getOverWriteReleases()
    {
        return this.overWriteReleases;
    }

    @Override
    public void setOverWriteReleases(boolean value)
    {
        this.overWriteReleases = value;
    }

    @Override
    public boolean getOverWriteSnapshots()
    {
        return this.overWriteSnapshots;
    }

    @Override
    public void setOverWriteSnapshots(boolean value)
    {
        this.overWriteSnapshots = value;
    }

    @Override
    public boolean getOverWriteIfNewer()
    {
        return this.overWriteIfNewer;
    }

    @Override
    public void setOverWriteIfNewer(boolean value)
    {
        this.overWriteIfNewer = value;
    }

    @Override
    public Directory getProjectBuildDirectory()
    {
        return this.projectBuildDirectory;
    }

    @Override
    public void setProjectBuildDirectory(Directory value)
    {
        this.projectBuildDirectory = value;
    }

    @Override
    public ArtifactFactory getFactory()
    {
        return this.factory;
    }

    @Override
    public ArtifactResolver getResolver()
    {
        return this.resolver;
    }

    @Override
    public ArtifactRepositoryContainer getRepositories()
    {
        return this.repositories;
    }

    @Override
    public void setRepositories(ArtifactRepositoryContainer value)
    {
        this.repositories = value;
    }

    @Override
    public ArchiverManager getArchiverManager()
    {
        return this.archiverManager;
    }

    @Override
    public void setArchiverManager(ArchiverManager value)
    {
        this.archiverManager = value;
    }

    @Override
    public List getReactorProjects()
    {
        return this.reactorProjects;
    }

    @Override
    public void setReactorProjects(List value)
    {
        this.reactorProjects = value;
    }

    @Override
    public boolean getSilent()
    {
        return this.silent;
    }

    @Override
    public void setSilent(boolean value)
    {
        this.silent = value;
    }

    @Override
    public DependencyGraphBuilder getDependencyGraphBuilder()
    {
        return this.dependencyGraphBuilder;
    }

    @Override
    public ArtifactHandlerManager getArtifactHandlerManager()
    {
        return this.artifactHandlerManager;
    }

    @Override
    public boolean getOutputAbsoluteArtifactFilename()
    {
        return this.outputAbsoluteArtifactFilename;
    }

    @Override
    public void setOutputAbsoluteArtifactFilename(boolean value)
    {
        this.outputAbsoluteArtifactFilename = value;
    }

    @Override
    public String getNarGroup()
    {
        return this.narGroup;
    }

    @Override
    public void setNarGroup(String value)
    {
        this.narGroup = value;
    }

    @Override
    public String getNarId()
    {
        return this.narId;
    }

    @Override
    public void setNarId(String value)
    {
        this.narId = value;
    }

    @Override
    public String getNarVersion()
    {
        return this.narVersion;
    }

    @Override
    public void setNarVersion(String value)
    {
        this.narVersion = value;
    }

    @Override
    public String getNarDependencyGroup()
    {
        return this.narDependencyGroup;
    }

    @Override
    public void setNarDependencyGroup(String value)
    {
        this.narDependencyGroup = value;
    }

    @Override
    public String getNarDependencyId()
    {
        return this.narDependencyId;
    }

    @Override
    public void setNarDependencyId(String value)
    {
        this.narDependencyId = value;
    }

    @Override
    public String getNarDependencyVersion()
    {
        return this.narDependencyVersion;
    }

    @Override
    public void setNarDependencyVersion(String value)
    {
        this.narDependencyVersion = value;
    }

    @Override
    public String getBuildTag()
    {
        return this.buildTag;
    }

    @Override
    public void setBuildTag(String value)
    {
        this.buildTag = value;
    }

    @Override
    public String getBuildBranch()
    {
        return this.buildBranch;
    }

    @Override
    public void setBuildBranch(String value)
    {
        this.buildBranch = value;
    }

    @Override
    public String getBuildRevision()
    {
        return this.buildRevision;
    }

    @Override
    public void setBuildRevision(String value)
    {
        this.buildRevision = value;
    }

    @Override
    public boolean getCloneDuringInstanceClassLoading()
    {
        return this.cloneDuringInstanceClassLoading;
    }

    @Override
    public void setCloneDuringInstanceClassLoading(boolean value)
    {
        this.cloneDuringInstanceClassLoading = value;
    }

    @Override
    public boolean getEnforceDocGeneration()
    {
        return this.enforceDocGeneration;
    }

    @Override
    public void setEnforceDocGeneration(boolean value)
    {
        this.enforceDocGeneration = value;
    }

    @Override
    public ProjectBuilder getProjectBuilder()
    {
        return this.projectBuilder;
    }
}