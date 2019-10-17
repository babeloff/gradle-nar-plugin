package org.babeloff.gradle.api.tasks.bundlings;

import java.util.HashSet;
import java.util.Set;

public class ExtensionDefinition
{
    public String getExtensionName()
    {
        return null;
    }

    public Set<ServiceAPIDefinition> getProvidedServiceAPIs()
    {
        return new HashSet<>();
    }
}
