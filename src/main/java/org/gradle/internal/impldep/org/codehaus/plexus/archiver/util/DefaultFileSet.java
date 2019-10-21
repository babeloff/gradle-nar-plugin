//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.util;

import java.io.File;

import org.gradle.internal.impldep.org.codehaus.plexus.archiver.FileSet;

public class DefaultFileSet extends AbstractFileSet implements FileSet
{
    private File directory;

    public DefaultFileSet() {
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return this.directory;
    }
}
