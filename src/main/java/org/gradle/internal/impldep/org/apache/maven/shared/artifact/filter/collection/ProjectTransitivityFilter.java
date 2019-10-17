//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

public class ProjectTransitivityFilter extends AbstractArtifactsFilter {
    private boolean excludeTransitive;
    private Set directDependencies;

    public ProjectTransitivityFilter(Set directDependencies, boolean excludeTransitive) {
        this.excludeTransitive = excludeTransitive;
        this.directDependencies = directDependencies;
    }

    // TODO: Implement gradle specific constructor
    public ProjectTransitivityFilter(DependencyHandler dependencies, boolean excludeTransitive)
    {
    }

    public Set filter(Set artifacts) {
        Set result = artifacts;
        if (this.excludeTransitive) {
            result = new HashSet();
            Iterator iterator = artifacts.iterator();

            while(iterator.hasNext()) {
                Artifact artifact = (Artifact)iterator.next();
                if (this.artifactIsADirectDependency(artifact)) {
                    ((Set)result).add(artifact);
                }
            }
        }

        return (Set)result;
    }

    public boolean artifactIsADirectDependency(Artifact artifact) {
        boolean result = false;
        Iterator iterator = this.directDependencies.iterator();

        while(iterator.hasNext()) {
            Artifact dependency = (Artifact)iterator.next();
            if (dependency.equals(artifact)) {
                result = true;
                break;
            }
        }

        return result;
    }

    public boolean isExcludeTransitive() {
        return this.excludeTransitive;
    }

    public void setExcludeTransitive(boolean excludeTransitive) {
        this.excludeTransitive = excludeTransitive;
    }
}
