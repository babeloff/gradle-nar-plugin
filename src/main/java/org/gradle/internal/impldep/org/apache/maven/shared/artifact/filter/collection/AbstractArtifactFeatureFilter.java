//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.codehaus.plexus.util.StringUtils;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;

public abstract class AbstractArtifactFeatureFilter extends AbstractArtifactsFilter {
    private List includes;
    private List excludes;

    public AbstractArtifactFeatureFilter(String include, String exclude) {
        this.setExcludes(exclude);
        this.setIncludes(include);
    }

    public Set filter(Set artifacts) {
        Set results = artifacts;
        if (this.includes != null && !this.includes.isEmpty()) {
            results = this.filterIncludes(artifacts, this.includes);
        }

        if (this.excludes != null && !this.excludes.isEmpty()) {
            results = this.filterExcludes(results, this.excludes);
        }

        return results;
    }

    private Set filterIncludes(Set artifacts, List theIncludes) {
        Set result = new HashSet();
        Iterator includeIter = theIncludes.iterator();

        while(includeIter.hasNext()) {
            String include = (String)includeIter.next();
            Iterator iter = artifacts.iterator();

            while(iter.hasNext()) {
                Artifact artifact = (Artifact)iter.next();
                if (this.compareFeatures(this.getArtifactFeature(artifact), include)) {
                    result.add(artifact);
                }
            }
        }

        return result;
    }

    private Set filterExcludes(Set artifacts, List theExcludes) {
        Set result = new HashSet();
        Iterator iter = artifacts.iterator();

        while(iter.hasNext()) {
            boolean exclude = false;
            Artifact artifact = (Artifact)iter.next();
            String artifactFeature = this.getArtifactFeature(artifact);
            Iterator excludeIter = theExcludes.iterator();

            while(excludeIter.hasNext()) {
                String excludeFeature = (String)excludeIter.next();
                if (this.compareFeatures(artifactFeature, excludeFeature)) {
                    exclude = true;
                    break;
                }
            }

            if (!exclude) {
                result.add(artifact);
            }
        }

        return result;
    }

    protected abstract String getArtifactFeature(Artifact var1);

    public void setExcludes(String excludeString) {
        if (StringUtils.isNotEmpty(excludeString)) {
            this.excludes = Arrays.asList(StringUtils.split(excludeString, ","));
        }

    }

    public void setIncludes(String includeString) {
        if (StringUtils.isNotEmpty(includeString)) {
            this.includes = Arrays.asList(StringUtils.split(includeString, ","));
        }

    }

    public List getExcludes() {
        return this.excludes;
    }

    public List getIncludes() {
        return this.includes;
    }

    protected boolean compareFeatures(String lhs, String rhs) {
        return lhs == null ? rhs == null : lhs.equals(rhs);
    }
}
