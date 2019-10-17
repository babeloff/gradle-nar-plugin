//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection;

import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

import java.util.Set;

public interface ArtifactsFilter {
    Set filter(Set var1) throws ArtifactFilterException;

    boolean isArtifactIncluded(Artifact var1) throws ArtifactFilterException;
}
