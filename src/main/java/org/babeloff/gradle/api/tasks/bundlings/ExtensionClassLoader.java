package org.babeloff.gradle.api.tasks.bundlings;


import java.net.URL;

public class ExtensionClassLoader extends ClassLoader
{
    public String getNiFiApiVersion()
    {
        return "";
    }

    public String toTree()
    {
        return "";
    }

    public Artifact getNarArtifact()
    {
        return null;
    }

    public Iterable<? extends URL> getURLs()
    {
        return null;
    }
}
