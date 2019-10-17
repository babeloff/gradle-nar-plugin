//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.resolvers;

import java.util.Set;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;
import org.gradle.internal.impldep.org.apache.maven.plugin.MojoExecutionException;
import org.gradle.internal.impldep.org.apache.maven.plugin.logging.Log;

public interface ArtifactsResolver {
    Set<Artifact> resolve(Set<Artifact> var1, Log var2) throws MojoExecutionException;
}
