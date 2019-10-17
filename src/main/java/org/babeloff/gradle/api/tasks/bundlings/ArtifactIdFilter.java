package org.babeloff.gradle.api.tasks.bundlings;

import org.gradle.api.provider.Property;

public class ArtifactIdFilter extends ProjectTransitivityFilter
{
    public ArtifactIdFilter(Property<String> includeArtifactIds, Property<String> excludeArtifactIds)
    {
        super(null, true);
    }
}
