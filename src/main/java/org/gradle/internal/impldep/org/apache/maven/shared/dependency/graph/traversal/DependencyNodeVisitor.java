//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.dependency.graph.traversal;


import org.gradle.internal.impldep.org.apache.maven.shared.dependency.graph.DependencyNode;

public interface DependencyNodeVisitor {
    boolean visit(DependencyNode var1);

    boolean endVisit(DependencyNode var1);
}
