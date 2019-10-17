//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.codehaus.plexus.util.StringUtils;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;
import org.gradle.internal.impldep.org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;

public class ScopeFilter extends AbstractArtifactsFilter {
    private String includeScope;
    private String excludeScope;

    public ScopeFilter(String includeScope, String excludeScope) {
        this.includeScope = includeScope;
        this.excludeScope = excludeScope;
    }

    public Set filter(Set artifacts) throws ArtifactFilterException {
        Set results = artifacts;
        ScopeArtifactFilter saf;
        Iterator iter;
        Artifact artifact;
        if (StringUtils.isNotEmpty(this.includeScope)) {
            if (!"compile".equals(this.includeScope) && !"test".equals(this.includeScope) && !"provided".equals(this.includeScope) && !"runtime".equals(this.includeScope) && !"system".equals(this.includeScope)) {
                throw new ArtifactFilterException("Invalid Scope in includeScope: " + this.includeScope);
            }

            results = new HashSet();
            if (!"provided".equals(this.includeScope) && !"system".equals(this.includeScope)) {
                saf = new ScopeArtifactFilter(this.includeScope);
                iter = artifacts.iterator();

                while(iter.hasNext()) {
                    artifact = (Artifact)iter.next();
                    if (saf.include(artifact)) {
                        ((Set)results).add(artifact);
                    }
                }
            } else {
                results = this.includeSingleScope(artifacts, this.includeScope);
            }
        } else if (StringUtils.isNotEmpty(this.excludeScope)) {
            if (!"compile".equals(this.excludeScope) && !"test".equals(this.excludeScope) && !"provided".equals(this.excludeScope) && !"runtime".equals(this.excludeScope) && !"system".equals(this.excludeScope)) {
                throw new ArtifactFilterException("Invalid Scope in excludeScope: " + this.excludeScope);
            }

            results = new HashSet();
            if ("test".equals(this.excludeScope)) {
                throw new ArtifactFilterException(" Can't exclude Test scope, this will exclude everything.");
            }

            if (!"provided".equals(this.excludeScope) && !"system".equals(this.excludeScope)) {
                saf = new ScopeArtifactFilter(this.excludeScope);
                iter = artifacts.iterator();

                while(iter.hasNext()) {
                    artifact = (Artifact)iter.next();
                    if (!saf.include(artifact)) {
                        ((Set)results).add(artifact);
                    }
                }
            } else {
                results = this.excludeSingleScope(artifacts, this.excludeScope);
            }
        }

        return (Set)results;
    }

    private Set includeSingleScope(Set artifacts, String scope) {
        HashSet results = new HashSet();
        Iterator iter = artifacts.iterator();

        while(iter.hasNext()) {
            Artifact artifact = (Artifact)iter.next();
            if (scope.equals(artifact.getScope())) {
                results.add(artifact);
            }
        }

        return results;
    }

    private Set excludeSingleScope(Set artifacts, String scope) {
        HashSet results = new HashSet();
        Iterator iter = artifacts.iterator();

        while(iter.hasNext()) {
            Artifact artifact = (Artifact)iter.next();
            if (!scope.equals(artifact.getScope())) {
                results.add(artifact);
            }
        }

        return results;
    }

    public String getIncludeScope() {
        return this.includeScope;
    }

    public void setIncludeScope(String scope) {
        this.includeScope = scope;
    }

    public String getExcludeScope() {
        return this.excludeScope;
    }

    public void setExcludeScope(String excludeScope) {
        this.excludeScope = excludeScope;
    }
}
