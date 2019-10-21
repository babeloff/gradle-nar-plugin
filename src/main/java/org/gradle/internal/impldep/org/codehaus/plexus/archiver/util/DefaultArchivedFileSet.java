//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.util;

import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchivedFileSet;

import java.io.File;

public class DefaultArchivedFileSet extends AbstractFileSet implements ArchivedFileSet
{
    private File archive;

    public DefaultArchivedFileSet() {
    }

    public void setArchive(File archive) {
        this.archive = archive;
    }

    public File getArchive() {
        return this.archive;
    }
}
