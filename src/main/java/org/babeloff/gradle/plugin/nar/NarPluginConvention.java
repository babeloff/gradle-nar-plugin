package org.babeloff.gradle.plugin.nar;

import org.gradle.api.Project;
import org.gradle.api.plugins.Convention;

import java.io.File;

/**
 * <p>A {@link Convention} used for the NarPlugin.</p>
 */
public abstract class NarPluginConvention {
    /**
     * Returns the web application directory.
     */
    public abstract File getNifiAppDir();

    /**
     * The name of the NiFi application directory, relative to the project directory.
     */
    public abstract String getNifiAppDirName();

    public abstract void setNifiAppDirName(String nifiAppDirName);

    public abstract Project getProject();
}
