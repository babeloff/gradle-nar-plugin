//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.archiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiverException;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;
import org.gradle.internal.impldep.org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.gradle.internal.impldep.org.apache.maven.execution.MavenSession;
import org.gradle.internal.impldep.org.apache.maven.project.MavenProject;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar.JarArchiver;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar.Manifest;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar.Manifest.Attribute;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar.Manifest.Section;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar.ManifestException;
import org.gradle.internal.impldep.org.codehaus.plexus.interpolation.*;
import org.gradle.internal.impldep.org.codehaus.plexus.util.StringUtils;

public class MavenArchiver {
    public static final String SIMPLE_LAYOUT = "${artifact.artifactId}-${artifact.version}${dashClassifier?}.${artifact.extension}";
    public static final String REPOSITORY_LAYOUT = "${artifact.groupIdPath}/${artifact.artifactId}/${artifact.baseVersion}/${artifact.artifactId}-${artifact.version}${dashClassifier?}.${artifact.extension}";
    public static final String SIMPLE_LAYOUT_NONUNIQUE = "${artifact.artifactId}-${artifact.baseVersion}${dashClassifier?}.${artifact.extension}";
    public static final String REPOSITORY_LAYOUT_NONUNIQUE = "${artifact.groupIdPath}/${artifact.artifactId}/${artifact.baseVersion}/${artifact.artifactId}-${artifact.baseVersion}${dashClassifier?}.${artifact.extension}";
    private static final List<String> ARTIFACT_EXPRESSION_PREFIXES;
    private JarArchiver archiver;
    private File archiveFile;

    public MavenArchiver() {
    }

    /** @deprecated */
    public Manifest getManifest(MavenProject project, MavenArchiveConfiguration config) throws ManifestException, DependencyResolutionRequiredException
    {
        return this.getManifest((MavenSession)null, project, (MavenArchiveConfiguration)config);
    }

    public Manifest getManifest(MavenSession session, MavenProject project, MavenArchiveConfiguration config) throws ManifestException, DependencyResolutionRequiredException {
        boolean hasManifestEntries = !config.isManifestEntriesEmpty();
        Map<String, String> entries = hasManifestEntries ? config.getManifestEntries() : Collections.EMPTY_MAP;
        Manifest manifest = this.getManifest(session, project, config.getManifest(), entries);
        Iterator i$;
        if (hasManifestEntries) {
            i$ = entries.entrySet().iterator();

            label54:
            while(true) {
                while(true) {
                    if (!i$.hasNext()) {
                        break label54;
                    }

                    Entry<String, String> entry = (Entry)i$.next();
                    String key = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    Attribute attr = manifest.getMainSection().getAttribute(key);
                    if (key.equals("Class-Path") && attr != null) {
                        attr.setValue(value + " " + attr.getValue());
                    } else {
                        this.addManifestAttribute(manifest, key, value);
                    }
                }
            }
        }

        Section theSection;
        if (!config.isManifestSectionsEmpty()) {
            for(i$ = config.getManifestSections().iterator(); i$.hasNext(); manifest.addConfiguredSection(theSection)) {
                ManifestSection section = (ManifestSection)i$.next();
                theSection = new Section();
                theSection.setName(section.getName());
                if (!section.isManifestEntriesEmpty()) {
                    Map<String, String> sectionEntries = section.getManifestEntries();
                    Iterator i$2 = sectionEntries.entrySet().iterator();

                    while(i$2.hasNext()) {
                        Entry<String, String> entry = (Entry)i$2.next();
                        String key = (String)entry.getKey();
                        String value = (String)entry.getValue();
                        Attribute attr = new Attribute(key, value);
                        theSection.addConfiguredAttribute(attr);
                    }
                }
            }
        }

        return manifest;
    }

    public Manifest getManifest(MavenProject project, ManifestConfiguration config) throws ManifestException, DependencyResolutionRequiredException {
        return this.getManifest((MavenSession)null, project, config, Collections.emptyMap());
    }

    public Manifest getManifest(MavenSession mavenSession, MavenProject project, ManifestConfiguration config) throws ManifestException, DependencyResolutionRequiredException {
        return this.getManifest(mavenSession, project, config, Collections.emptyMap());
    }

    private void addManifestAttribute(Manifest manifest, Map<String, String> map, String key, String value) throws ManifestException {
        if (!map.containsKey(key)) {
            this.addManifestAttribute(manifest, key, value);
        }
    }

    private void addManifestAttribute(Manifest manifest, String key, String value) throws ManifestException {
        Attribute attr;
        if (!StringUtils.isEmpty(value)) {
            attr = new Attribute(key, value);
            manifest.addConfiguredAttribute(attr);
        } else {
            attr = new Attribute(key, "");
            manifest.addConfiguredAttribute(attr);
        }

    }

    protected Manifest getManifest(MavenSession session, MavenProject project, ManifestConfiguration config, Map<String, String> entries) throws ManifestException, DependencyResolutionRequiredException {
        Manifest m = new Manifest();
        this.addCreatedByEntry(session, m, entries);
        this.addCustomEntries(m, entries, config);
        String ename;
        if (config.isAddClasspath()) {
            StringBuilder classpath = new StringBuilder();
            List<String> artifacts = project.getRuntimeClasspathElements();
            String classpathPrefix = config.getClasspathPrefix();
            String layoutType = config.getClasspathLayoutType();
            String layout = config.getCustomClasspathLayout();
            Interpolator interpolator = new StringSearchInterpolator();
            Iterator i$ = artifacts.iterator();

            label332:
            while(true) {
                while(true) {
                    File f;
                    do {
                        if (!i$.hasNext()) {
                            if (classpath.length() > 0) {
                                this.addManifestAttribute(m, "Class-Path", classpath.toString());
                            }
                            break label332;
                        }

                        ename = (String)i$.next();
                        f = new File(ename);
                    } while(!f.getAbsoluteFile().isFile());

                    Artifact artifact = this.findArtifactWithFile(project.getArtifacts(), f);
                    if (classpath.length() > 0) {
                        classpath.append(" ");
                    }

                    classpath.append(classpathPrefix);
                    if (artifact != null && layoutType != null) {
                        List<ValueSource> valueSources = new ArrayList();
                        valueSources.add(new PrefixedObjectValueSource(ARTIFACT_EXPRESSION_PREFIXES, artifact, true));
                        valueSources.add(new PrefixedObjectValueSource(ARTIFACT_EXPRESSION_PREFIXES, artifact == null ? null : artifact.getArtifactHandler(), true));
                        Properties extraExpressions = new Properties();
                        if (artifact != null) {
                            if (!artifact.isSnapshot()) {
                                extraExpressions.setProperty("baseVersion", artifact.getVersion());
                            }

                            extraExpressions.setProperty("groupIdPath", artifact.getGroupId().replace('.', '/'));
                            if (StringUtils.isNotEmpty(artifact.getClassifier())) {
                                extraExpressions.setProperty("dashClassifier", "-" + artifact.getClassifier());
                                extraExpressions.setProperty("dashClassifier?", "-" + artifact.getClassifier());
                            } else {
                                extraExpressions.setProperty("dashClassifier", "");
                                extraExpressions.setProperty("dashClassifier?", "");
                            }
                        }

                        valueSources.add(new PrefixedPropertiesValueSource(ARTIFACT_EXPRESSION_PREFIXES, extraExpressions, true));
                        Iterator i$2 = valueSources.iterator();

                        while(i$2.hasNext()) {
                            ValueSource vs = (ValueSource)i$2.next();
                            interpolator.addValueSource(vs);
                        }

                        PrefixAwareRecursionInterceptor recursionInterceptor = new PrefixAwareRecursionInterceptor(ARTIFACT_EXPRESSION_PREFIXES);
                        boolean var26 = false;

                        try {
                            var26 = true;
                            if ("simple".equals(layoutType)) {
                                if (config.isUseUniqueVersions()) {
                                    classpath.append(interpolator.interpolate("${artifact.artifactId}-${artifact.version}${dashClassifier?}.${artifact.extension}", recursionInterceptor));
                                    var26 = false;
                                } else {
                                    classpath.append(interpolator.interpolate("${artifact.artifactId}-${artifact.baseVersion}${dashClassifier?}.${artifact.extension}", recursionInterceptor));
                                    var26 = false;
                                }
                            } else if ("repository".equals(layoutType)) {
                                if (config.isUseUniqueVersions()) {
                                    classpath.append(interpolator.interpolate("${artifact.groupIdPath}/${artifact.artifactId}/${artifact.baseVersion}/${artifact.artifactId}-${artifact.version}${dashClassifier?}.${artifact.extension}", recursionInterceptor));
                                    var26 = false;
                                } else {
                                    classpath.append(interpolator.interpolate("${artifact.groupIdPath}/${artifact.artifactId}/${artifact.baseVersion}/${artifact.artifactId}-${artifact.baseVersion}${dashClassifier?}.${artifact.extension}", recursionInterceptor));
                                    var26 = false;
                                }
                            } else {
                                if (!"custom".equals(layoutType)) {
                                    throw new ManifestException("Unknown classpath layout type: '" + layoutType + "'. Check your <archive><manifest><layoutType/> element.");
                                }

                                if (layout == null) {
                                    throw new ManifestException("custom layout type was declared, but custom layout expression was not specified. Check your <archive><manifest><customLayout/> element.");
                                }

                                classpath.append(interpolator.interpolate(layout, recursionInterceptor));
                                var26 = false;
                            }
                        } catch (InterpolationException var27) {
                            ManifestException error = new ManifestException("Error interpolating artifact path for classpath entry: " + var27.getMessage());
                            error.initCause(var27);
                            throw error;
                        } finally {
                            if (var26) {
                                Iterator i$4 = valueSources.iterator();

                                while(i$4.hasNext()) {
                                    ValueSource vs = (ValueSource)i$4.next();
                                    interpolator.removeValuesSource(vs);
                                }

                            }
                        }

                        Iterator i$3 = valueSources.iterator();

                        while(i$3.hasNext()) {
                            ValueSource vs = (ValueSource)i$3.next();
                            interpolator.removeValuesSource(vs);
                        }
                    } else {
                        classpath.append(f.getName());
                    }
                }
            }
        }

        if (config.isAddDefaultSpecificationEntries()) {
            this.addManifestAttribute(m, entries, "Specification-Title", project.getName());
            this.addManifestAttribute(m, entries, "Specification-Version", project.getVersion());
            if (project.getOrganization() != null) {
                this.addManifestAttribute(m, entries, "Specification-Vendor", project.getOrganization().getName());
            }
        }

        if (config.isAddDefaultImplementationEntries()) {
            this.addManifestAttribute(m, entries, "Implementation-Title", project.getName());
            this.addManifestAttribute(m, entries, "Implementation-Version", project.getVersion());
            this.addManifestAttribute(m, entries, "Implementation-Vendor-Id", project.getGroupId());
            if (project.getOrganization() != null) {
                this.addManifestAttribute(m, entries, "Implementation-Vendor", project.getOrganization().getName());
            }
        }

        String mainClass = config.getMainClass();
        if (mainClass != null && !"".equals(mainClass)) {
            this.addManifestAttribute(m, entries, "Main-Class", mainClass);
        }

        if (config.isAddExtensions()) {
            StringBuilder extensionsList = new StringBuilder();
            Set<Artifact> artifacts = project.getArtifacts();
            Iterator i$ = artifacts.iterator();

            Artifact artifact1;
            while(i$.hasNext()) {
                artifact1 = (Artifact)i$.next();
                if (!"test".equals(artifact1.getScope()) && "jar".equals(artifact1.getType())) {
                    if (extensionsList.length() > 0) {
                        extensionsList.append(" ");
                    }

                    extensionsList.append(artifact1.getArtifactId());
                }
            }

            if (extensionsList.length() > 0) {
                this.addManifestAttribute(m, entries, "Extension-List", extensionsList.toString());
            }

            i$ = artifacts.iterator();

            while(i$.hasNext()) {
                artifact1 = (Artifact)i$.next();
                Artifact artifact = (Artifact)artifact1;
                if ("jar".equals(artifact.getType())) {
                    String artifactId = artifact.getArtifactId().replace('.', '_');
                    ename = artifactId + "-Extension-Name";
                    this.addManifestAttribute(m, entries, ename, artifact.getArtifactId());
                    String iname = artifactId + "-Implementation-Version";
                    this.addManifestAttribute(m, entries, iname, artifact.getVersion());
                    if (artifact.getRepository() != null) {
                        iname = artifactId + "-Implementation-URL";
                        String url = artifact.getRepository().getUrl() + "/" + artifact.toString();
                        this.addManifestAttribute(m, entries, iname, url);
                    }
                }
            }
        }

        return m;
    }

    private void addCustomEntries(Manifest m, Map<String, String> entries, ManifestConfiguration config) throws ManifestException {
        this.addManifestAttribute(m, entries, "Built-By", System.getProperty("user.name"));
        this.addManifestAttribute(m, entries, "Build-Jdk", System.getProperty("java.version"));
        if (config.getPackageName() != null) {
            this.addManifestAttribute(m, entries, "Package", config.getPackageName());
        }

    }

    public JarArchiver getArchiver() {
        return this.archiver;
    }

    public void setArchiver(JarArchiver archiver) {
        this.archiver = archiver;
    }

    public void setOutputFile(File outputFile) {
        this.archiveFile = outputFile;
    }

    /** @deprecated */
    public void createArchive(MavenProject project, MavenArchiveConfiguration archiveConfiguration) throws ArchiverException, ManifestException, IOException, DependencyResolutionRequiredException {
        this.createArchive((MavenSession)null, project, archiveConfiguration);
    }

    public void createArchive(MavenSession session, MavenProject project, MavenArchiveConfiguration archiveConfiguration) throws ArchiverException, ManifestException, IOException, DependencyResolutionRequiredException {
        MavenProject workingProject = new MavenProject(project);
        boolean forced = archiveConfiguration.isForced();
        if (archiveConfiguration.isAddMavenDescriptor()) {
            if (workingProject.getArtifact().isSnapshot()) {
                workingProject.setVersion(workingProject.getArtifact().getVersion());
            }

            String groupId = workingProject.getGroupId();
            String artifactId = workingProject.getArtifactId();
            this.archiver.addFile(project.getFile(), "META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml");
            File pomPropertiesFile = archiveConfiguration.getPomPropertiesFile();
            if (pomPropertiesFile == null) {
                File dir = new File(workingProject.getBuild().getDirectory(), "maven-archiver");
                pomPropertiesFile = new File(dir, "pom.properties");
            }

            (new PomPropertiesUtil()).createPomProperties(workingProject, this.archiver, pomPropertiesFile, forced);
        }

        File manifestFile = archiveConfiguration.getManifestFile();
        if (manifestFile != null) {
            this.archiver.setManifest(manifestFile);
        }

        Manifest manifest = this.getManifest(session, workingProject, archiveConfiguration);
        this.archiver.addConfiguredManifest(manifest);
        this.archiver.setCompress(archiveConfiguration.isCompress());
        this.archiver.setIndex(archiveConfiguration.isIndex());
        this.archiver.setDestFile(this.archiveFile);
        if (archiveConfiguration.getManifest().isAddClasspath()) {
            List<String> artifacts = project.getRuntimeClasspathElements();
            Iterator i$ = artifacts.iterator();

            while(i$.hasNext()) {
                String artifact = (String)i$.next();
                File f = new File(artifact);
                this.archiver.addConfiguredIndexJars(f);
            }
        }

        this.archiver.setForced(forced);
        if (!archiveConfiguration.isForced() && this.archiver.isSupportingForced()) {
        }

        this.archiver.createArchive();
    }

    private void addCreatedByEntry(MavenSession session, Manifest m, Map entries) throws ManifestException {
        String createdBy = "Apache Maven";
        if (session != null) {
            String mavenVersion = session.getExecutionProperties().getProperty("maven.version");
            if (mavenVersion != null) {
                createdBy = createdBy + " " + mavenVersion;
            }
        }

        this.addManifestAttribute(m, entries, "Created-By", createdBy);
    }

    private Artifact findArtifactWithFile(Set<Artifact> artifacts, File file) {
        Iterator i$ = artifacts.iterator();

        Artifact artifact;
        do {
            if (!i$.hasNext()) {
                return null;
            }

            artifact = (Artifact)i$.next();
        } while(artifact.getFile() == null || !artifact.getFile().equals(file));

        return artifact;
    }

    static {
        List<String> artifactExpressionPrefixes = new ArrayList();
        artifactExpressionPrefixes.add("artifact.");
        ARTIFACT_EXPRESSION_PREFIXES = artifactExpressionPrefixes;
    }
}
