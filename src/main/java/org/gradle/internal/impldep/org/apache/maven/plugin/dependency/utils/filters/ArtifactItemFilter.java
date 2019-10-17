//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.filters;

import org.gradle.internal.impldep.org.apache.maven.plugin.dependency.fromConfiguration.ArtifactItem;
import org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;

public interface ArtifactItemFilter {
    boolean isArtifactIncluded(ArtifactItem var1) throws ArtifactFilterException;
}
