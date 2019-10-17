//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.dependency.graph;

import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.gradle.internal.impldep.org.apache.maven.project.MavenProject;
import org.gradle.internal.impldep.org.apache.maven.project.ProjectBuildingRequest;

import java.util.Collection;

public interface DependencyGraphBuilder {
    DependencyNode buildDependencyGraph(ProjectBuildingRequest var1, ArtifactFilter var2) throws DependencyGraphBuilderException;

    DependencyNode buildDependencyGraph(ProjectBuildingRequest var1, ArtifactFilter var2, Collection<MavenProject> var3) throws DependencyGraphBuilderException;
}
