package org.babeloff.gradle.api.plugins;
/**
 * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPluginConvention.java
 */

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
