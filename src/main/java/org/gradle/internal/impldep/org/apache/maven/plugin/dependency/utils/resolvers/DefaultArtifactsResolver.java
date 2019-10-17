//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.resolvers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;
import org.gradle.internal.impldep.org.apache.maven.artifact.repository.ArtifactRepository;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.ArtifactResolver;
import org.gradle.internal.impldep.org.apache.maven.plugin.MojoExecutionException;
import org.gradle.internal.impldep.org.apache.maven.plugin.logging.Log;

public class DefaultArtifactsResolver implements ArtifactsResolver {
    ArtifactResolver resolver;
    ArtifactRepository local;
    List<ArtifactRepository> remoteRepositories;
    boolean stopOnFailure;

    public DefaultArtifactsResolver(ArtifactResolver theResolver, ArtifactRepository theLocal, List<ArtifactRepository> theRemoteRepositories, boolean theStopOnFailure) {
        this.resolver = theResolver;
        this.local = theLocal;
        this.remoteRepositories = theRemoteRepositories;
        this.stopOnFailure = theStopOnFailure;
    }

    public Set<Artifact> resolve(Set<Artifact> artifacts, Log log) throws MojoExecutionException
    {
        Set<Artifact> resolvedArtifacts = new HashSet();
        Iterator i$ = artifacts.iterator();

        while(i$.hasNext()) {
            Artifact artifact = (Artifact)i$.next();

            try {
                this.resolver.resolve(artifact, this.remoteRepositories, this.local);
                resolvedArtifacts.add(artifact);
            } catch (ArtifactResolutionException var7) {
                log.debug("error resolving: " + artifact.getId());
                log.debug(var7);
                if (this.stopOnFailure) {
                    throw new MojoExecutionException("error resolving: " + artifact.getId(), var7);
                }
            } catch (ArtifactNotFoundException var8) {
                log.debug("not found in any repository: " + artifact.getId());
                if (this.stopOnFailure) {
                    throw new MojoExecutionException("not found in any repository: " + artifact.getId(), var8);
                }
            }
        }

        return resolvedArtifacts;
    }
}
