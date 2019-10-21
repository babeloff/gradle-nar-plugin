//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.filters;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.fromConfiguration.ArtifactItem;
import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection.AbstractArtifactsFilter;
import org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.gradle.internal.impldep.org.codehaus.plexus.util.StringUtils;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

public class DestFileFilter extends AbstractArtifactsFilter implements ArtifactItemFilter {
    private boolean overWriteReleases;
    private boolean overWriteSnapshots;
    private boolean overWriteIfNewer;
    private boolean useSubDirectoryPerArtifact;
    private boolean useSubDirectoryPerType;
    private boolean useSubDirectoryPerScope;
    private boolean useRepositoryLayout;
    private boolean removeVersion;
    private boolean removeClassifier;
    private File outputFileDirectory;

    public DestFileFilter(File outputFileDirectory) {
        this.outputFileDirectory = outputFileDirectory;
        this.overWriteReleases = false;
        this.overWriteIfNewer = false;
        this.overWriteSnapshots = false;
        this.useSubDirectoryPerArtifact = false;
        this.useSubDirectoryPerType = false;
        this.useSubDirectoryPerScope = false;
        this.removeVersion = false;
        this.removeClassifier = false;
    }

    public DestFileFilter(boolean overWriteReleases, boolean overWriteSnapshots, boolean overWriteIfNewer, boolean useSubDirectoryPerArtifact, boolean useSubDirectoryPerType, boolean useSubDirectoryPerScope, boolean useRepositoryLayout, boolean removeVersion, File outputFileDirectory) {
        this.overWriteReleases = overWriteReleases;
        this.overWriteSnapshots = overWriteSnapshots;
        this.overWriteIfNewer = overWriteIfNewer;
        this.useSubDirectoryPerArtifact = useSubDirectoryPerArtifact;
        this.useSubDirectoryPerType = useSubDirectoryPerType;
        this.useSubDirectoryPerScope = useSubDirectoryPerScope;
        this.useRepositoryLayout = useRepositoryLayout;
        this.removeVersion = removeVersion;
        this.outputFileDirectory = outputFileDirectory;
    }

    public Set filter(Set artifacts) throws ArtifactFilterException
    {
        Set<Artifact> result = new HashSet();
        Iterator i$ = artifacts.iterator();

        while(i$.hasNext()) {
            Artifact artifact = (Artifact)i$.next();
            if (this.isArtifactIncluded(new ArtifactItem(artifact))) {
                result.add(artifact);
            }
        }

        return result;
    }

    public boolean isOverWriteReleases() {
        return this.overWriteReleases;
    }

    public void setOverWriteReleases(boolean overWriteReleases) {
        this.overWriteReleases = overWriteReleases;
    }

    public boolean isOverWriteSnapshots() {
        return this.overWriteSnapshots;
    }

    public void setOverWriteSnapshots(boolean overWriteSnapshots) {
        this.overWriteSnapshots = overWriteSnapshots;
    }

    public boolean isOverWriteIfNewer() {
        return this.overWriteIfNewer;
    }

    public void setOverWriteIfNewer(boolean overWriteIfNewer) {
        this.overWriteIfNewer = overWriteIfNewer;
    }

    public File getOutputFileDirectory() {
        return this.outputFileDirectory;
    }

    public void setOutputFileDirectory(File outputFileDirectory) {
        this.outputFileDirectory = outputFileDirectory;
    }

    public boolean isRemoveVersion() {
        return this.removeVersion;
    }

    public void setRemoveVersion(boolean removeVersion) {
        this.removeVersion = removeVersion;
    }

    public boolean isRemoveClassifier() {
        return this.removeClassifier;
    }

    public void setRemoveClassifier(boolean removeClassifier) {
        this.removeClassifier = removeClassifier;
    }

    public boolean isUseSubDirectoryPerArtifact() {
        return this.useSubDirectoryPerArtifact;
    }

    public void setUseSubDirectoryPerArtifact(boolean useSubDirectoryPerArtifact) {
        this.useSubDirectoryPerArtifact = useSubDirectoryPerArtifact;
    }

    public boolean isUseSubDirectoryPerType() {
        return this.useSubDirectoryPerType;
    }

    public void setUseSubDirectoryPerType(boolean useSubDirectoryPerType) {
        this.useSubDirectoryPerType = useSubDirectoryPerType;
    }

    public boolean isUseRepositoryLayout() {
        return this.useRepositoryLayout;
    }

    public void setUseRepositoryLayout(boolean useRepositoryLayout) {
        this.useRepositoryLayout = useRepositoryLayout;
    }

    public boolean isArtifactIncluded(ArtifactItem item) {
        Artifact artifact = item.getArtifact();
        boolean overWrite = artifact.isSnapshot() && this.overWriteSnapshots || !artifact.isSnapshot() && this.overWriteReleases;
        File destFolder = item.getOutputDirectory();
        if (destFolder == null) {
            destFolder = DependencyUtil.getFormattedOutputDirectory(this.useSubDirectoryPerScope, this.useSubDirectoryPerType, this.useSubDirectoryPerArtifact, this.useRepositoryLayout, this.removeVersion, this.outputFileDirectory, artifact);
        }

        File destFile;
        if (StringUtils.isEmpty(item.getDestFileName())) {
            destFile = new File(destFolder, DependencyUtil.getFormattedFileName(artifact, this.removeVersion));
        } else {
            destFile = new File(destFolder, item.getDestFileName());
        }

        return overWrite || !destFile.exists() || this.overWriteIfNewer && artifact.getFile().lastModified() > destFile.lastModified();
    }
}
