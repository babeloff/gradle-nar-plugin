//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.dependency.graph;

import java.util.List;
import org.gradle.internal.impldep.org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

public interface DependencyNode {
    Artifact getArtifact();

    List<DependencyNode> getChildren();

    boolean accept(DependencyNodeVisitor var1);

    DependencyNode getParent();

    String getPremanagedVersion();

    String getPremanagedScope();

    String getVersionConstraint();

    String toNodeString();

    Boolean getOptional();
}
