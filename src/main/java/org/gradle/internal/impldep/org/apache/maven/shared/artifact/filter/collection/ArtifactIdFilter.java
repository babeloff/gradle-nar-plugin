//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection;


import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

public class ArtifactIdFilter extends AbstractArtifactFeatureFilter {
    public ArtifactIdFilter(String include, String exclude) {
        super(include, exclude);
    }

    protected String getArtifactFeature(Artifact artifact) {
        return artifact.getArtifactId();
    }
}
