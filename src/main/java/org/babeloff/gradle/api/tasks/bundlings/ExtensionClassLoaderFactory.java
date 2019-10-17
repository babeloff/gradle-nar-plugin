package org.babeloff.gradle.api.tasks.bundlings;

import org.apache.log4j.Logger;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder.DependencyGraphBuilder;
import org.gradle.api.provider.Property;
import org.gradle.internal.impldep.org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactResolver;
import org.gradle.internal.impldep.org.apache.maven.project.ProjectBuilder;

public class ExtensionClassLoaderFactory
{
    public ExtensionClassLoader createExtensionClassLoader()
    {
        return null;
    }

    public static class Builder
    {
        public Builder artifactResolver(Property<ArtifactResolver> resolver) {
            return this;
        }

        public Builder dependencyGraphBuilder(DependencyGraphBuilder dependencyGraphBuilder)
        {
            return this;
        }

        public Builder localRepository(ArtifactRepositoryContainer artifactRepositories)
        {
            return this;
        }

        public Builder log(Logger logger)
        {
            return this;
        }

        public Builder project(Project project)
        {
            return this;
        }

        public Builder projectBuilder(Property<ProjectBuilder> projectBuilder)
        {
            return this;
        }

        public Builder artifactHandlerManager(Property<ArtifactHandlerManager> manager)
        {
            return this;
        }

        public ExtensionClassLoaderFactory build()
        {
            return null;
        }
    }
}
