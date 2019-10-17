package org.babeloff.gradle.api.tasks.bundlings;

import org.babeloff.gradle.api.conventions.MavenArchiveConfiguration;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.gradle.api.Project;

import java.io.File;

public class MavenArchiver
{
    public void setOutputFile(File narFile)
    {
    }

    public void setArchiver(JarArchiver jarArchiver)
    {
    }

    public void setForced(Boolean aBoolean)
    {
    }

    public Archiver getArchiver()
    {
        return null;
    }

    public void createArchive(Project project, MavenArchiveConfiguration archive)
    {
    }
}
