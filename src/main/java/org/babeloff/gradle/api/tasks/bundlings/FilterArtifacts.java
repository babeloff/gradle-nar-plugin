package org.babeloff.gradle.api.tasks.bundlings;

import org.gradle.api.artifacts.dsl.ArtifactHandler;

import java.util.Set;

public class FilterArtifacts
{
    public void addFilter(ProjectTransitivityFilter projectTransitivityFilter)
    {
        return ;
    }


    public void addFilter(ScopeFilter scopeFilter)
    {
        return ;
    }

    public void addFilter(TypeFilter nar)
    {
    }

    public ArtifactHandler filter(ArtifactHandler nar) throws ArtifactFilterException
    {
        return nar;
    }

    public void addFilter(ArtifactsFilter nar)
    {
        return ;
    }


    public void clearFilters()
    {
    }

    public Set filter(Set artifacts)
    {
        return artifacts;
    }
}
