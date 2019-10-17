//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.shared.artifact.filter.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class FilterArtifacts {
    private ArrayList filters = new ArrayList();

    public FilterArtifacts() {
        this.filters = new ArrayList();
    }

    public void clearFilters() {
        this.filters.clear();
    }

    public void addFilter(ArtifactsFilter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }

    }

    public void addFilter(int index, ArtifactsFilter filter) {
        if (filter != null) {
            this.filters.add(index, filter);
        }

    }

    public Set filter(Set artifacts) throws ArtifactFilterException {
        Iterator filterIterator = this.filters.iterator();

        while(filterIterator.hasNext()) {
            ArtifactsFilter filter = (ArtifactsFilter)filterIterator.next();

            try {
                artifacts = filter.filter(artifacts);
            } catch (NullPointerException var5) {
            }
        }

        return artifacts;
    }

    public ArrayList getFilters() {
        return this.filters;
    }

    public void setFilters(ArrayList filters) {
        this.filters = filters;
    }

}
