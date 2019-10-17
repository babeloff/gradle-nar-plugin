//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection;

import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractArtifactsFilter implements ArtifactsFilter
{
    public AbstractArtifactsFilter() {
    }

    public boolean isArtifactIncluded(Artifact artifact) throws ArtifactFilterException
    {
        Set set = new HashSet();
        set.add(artifact);

        Set fset = this.filter(set);
        return fset.contains(artifact);
    }
}
