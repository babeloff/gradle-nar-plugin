//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.plugin.dependency.fromConfiguration;

import java.io.File;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.codehaus.plexus.util.StringUtils;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

public class ArtifactItem {
    private String groupId;
    private String artifactId;
    private String version = null;
    private String baseVersion = null;
    private String type = "jar";
    private String classifier;
    private File outputDirectory;
    private String destFileName;
    private String overWrite;
    private boolean needsProcessing;
    private Artifact artifact;
    private String includes;
    private String excludes;

    public ArtifactItem() {
    }

    public ArtifactItem(Artifact artifact) {
        this.setArtifact(artifact);
        this.setArtifactId(artifact.getArtifactId());
        this.setClassifier(artifact.getClassifier());
        this.setGroupId(artifact.getGroupId());
        this.setType(artifact.getType());
        this.setVersion(artifact.getVersion());
        this.setBaseVersion(artifact.getBaseVersion());
    }

    private String filterEmptyString(String in) {
        return "".equals(in) ? null : in;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public void setArtifactId(String artifact) {
        this.artifactId = this.filterEmptyString(artifact);
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = this.filterEmptyString(groupId);
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = this.filterEmptyString(type);
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = this.filterEmptyString(version);
    }

    public String getBaseVersion() {
        return this.baseVersion;
    }

    public void setBaseVersion(String baseVersion) {
        this.baseVersion = this.filterEmptyString(baseVersion);
    }

    public String getClassifier() {
        return this.classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = this.filterEmptyString(classifier);
    }

    public String toString() {
        return this.classifier == null ? this.groupId + ":" + this.artifactId + ":" + StringUtils.defaultString(this.version, "?") + ":" + this.type : this.groupId + ":" + this.artifactId + ":" + this.classifier + ":" + StringUtils.defaultString(this.version, "?") + ":" + this.type;
    }

    public File getOutputDirectory() {
        return this.outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getDestFileName() {
        return this.destFileName;
    }

    public void setDestFileName(String destFileName) {
        this.destFileName = this.filterEmptyString(destFileName);
    }

    public boolean isNeedsProcessing() {
        return this.needsProcessing;
    }

    public void setNeedsProcessing(boolean needsProcessing) {
        this.needsProcessing = needsProcessing;
    }

    public String getOverWrite() {
        return this.overWrite;
    }

    public void setOverWrite(String overWrite) {
        this.overWrite = overWrite;
    }

    public Artifact getArtifact() {
        return this.artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public String getExcludes() {
        return DependencyUtil.cleanToBeTokenizedString(this.excludes);
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public String getIncludes() {
        return DependencyUtil.cleanToBeTokenizedString(this.includes);
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }
}
