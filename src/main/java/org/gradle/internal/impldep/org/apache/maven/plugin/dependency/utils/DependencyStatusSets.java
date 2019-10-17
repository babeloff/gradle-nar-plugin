//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils;

import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DependencyStatusSets {
    Set<Artifact> resolvedDependencies = null;
    Set<Artifact> unResolvedDependencies = null;
    Set<Artifact> skippedDependencies = null;

    public DependencyStatusSets() {
    }

    public DependencyStatusSets(Set<Artifact> resolved, Set<Artifact> unResolved, Set<Artifact> skipped) {
        if (resolved != null) {
            this.resolvedDependencies = new LinkedHashSet(resolved);
        }

        if (unResolved != null) {
            this.unResolvedDependencies = new LinkedHashSet(unResolved);
        }

        if (skipped != null) {
            this.skippedDependencies = new LinkedHashSet(skipped);
        }

    }

    public Set<Artifact> getResolvedDependencies() {
        return this.resolvedDependencies;
    }

    public void setResolvedDependencies(Set<Artifact> resolvedDependencies) {
        if (resolvedDependencies != null) {
            this.resolvedDependencies = new LinkedHashSet(resolvedDependencies);
        } else {
            this.resolvedDependencies = null;
        }

    }

    public Set<Artifact> getSkippedDependencies() {
        return this.skippedDependencies;
    }

    public void setSkippedDependencies(Set<Artifact> skippedDependencies) {
        if (skippedDependencies != null) {
            this.skippedDependencies = new LinkedHashSet(skippedDependencies);
        } else {
            this.skippedDependencies = null;
        }

    }

    public Set<Artifact> getUnResolvedDependencies() {
        return this.unResolvedDependencies;
    }

    public void setUnResolvedDependencies(Set<Artifact> unResolvedDependencies) {
        if (unResolvedDependencies != null) {
            this.unResolvedDependencies = new LinkedHashSet(unResolvedDependencies);
        } else {
            this.unResolvedDependencies = null;
        }

    }

    public String getOutput(boolean outputAbsoluteArtifactFilename) {
        return this.getOutput(outputAbsoluteArtifactFilename, true);
    }

    public String getOutput(boolean outputAbsoluteArtifactFilename, boolean outputScope) {
        return this.getOutput(outputAbsoluteArtifactFilename, outputScope, false);
    }

    public String getOutput(boolean outputAbsoluteArtifactFilename, boolean outputScope, boolean sort) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("The following files have been resolved:\n");
        if (this.resolvedDependencies != null && !this.resolvedDependencies.isEmpty()) {
            sb.append(this.buildArtifactListOutput(this.resolvedDependencies, outputAbsoluteArtifactFilename, outputScope, sort));
        } else {
            sb.append("   none\n");
        }

        LinkedHashSet unResolvedDependencies;
        if (this.skippedDependencies != null && !this.skippedDependencies.isEmpty()) {
            sb.append("\n");
            sb.append("The following files were skipped:\n");
            unResolvedDependencies = new LinkedHashSet();
            unResolvedDependencies.addAll(this.skippedDependencies);
            sb.append(this.buildArtifactListOutput(unResolvedDependencies, outputAbsoluteArtifactFilename, outputScope, sort));
        }

        if (this.unResolvedDependencies != null && !this.unResolvedDependencies.isEmpty()) {
            sb.append("\n");
            sb.append("The following files have NOT been resolved:\n");
            unResolvedDependencies = new LinkedHashSet();
            unResolvedDependencies.addAll(this.unResolvedDependencies);
            sb.append(this.buildArtifactListOutput(unResolvedDependencies, outputAbsoluteArtifactFilename, outputScope, sort));
        }

        sb.append("\n");
        return sb.toString();
    }

    private StringBuilder buildArtifactListOutput(Set<Artifact> artifacts, boolean outputAbsoluteArtifactFilename, boolean outputScope, boolean sort) {
        StringBuilder sb = new StringBuilder();
        List<String> artifactStringList = new ArrayList();
        Iterator i$ = artifacts.iterator();

        while(i$.hasNext()) {
            Artifact artifact = (Artifact)i$.next();
            String artifactFilename = null;
            if (outputAbsoluteArtifactFilename) {
                try {
                    artifactFilename = artifact.getFile().getAbsoluteFile().getPath();
                } catch (NullPointerException var11) {
                    artifactFilename = null;
                }
            }

            String id = outputScope ? artifact.toString() : artifact.getId();
            artifactStringList.add("   " + id + (outputAbsoluteArtifactFilename ? ":" + artifactFilename : "") + "\n");
        }

        if (sort) {
            Collections.sort(artifactStringList);
        }

        i$ = artifactStringList.iterator();

        while(i$.hasNext()) {
            String artifactString = (String)i$.next();
            sb.append(artifactString);
        }

        return sb;
    }
}
