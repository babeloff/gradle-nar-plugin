//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection;


import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

public class GroupIdFilter extends AbstractArtifactFeatureFilter {
    public GroupIdFilter(String include, String exclude) {
        super(include, exclude);
    }

    protected String getArtifactFeature(Artifact artifact) {
        return artifact.getGroupId();
    }

    protected boolean compareFeatures(String lhs, String rhs) {
        return lhs.startsWith(rhs);
    }
}
