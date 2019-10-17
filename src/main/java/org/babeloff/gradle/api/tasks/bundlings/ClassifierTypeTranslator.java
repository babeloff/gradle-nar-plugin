package org.babeloff.gradle.api.tasks.bundlings;

import org.gradle.api.provider.Property;
import org.gradle.internal.impldep.org.apache.maven.artifact.factory.ArtifactFactory;

public class ClassifierTypeTranslator extends ArtifactTranslator
{
    public ClassifierTypeTranslator(String s, Property<String> type, Property<ArtifactFactory> factory)
    {
        super();
    }
}
