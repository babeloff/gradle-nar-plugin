//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public interface ArchiveFile {
    Enumeration getEntries() throws IOException;

    InputStream getInputStream(ArchiveFile.Entry var1) throws IOException;

    public interface Entry {
        String getName();

        boolean isDirectory();

        long getLastModificationTime();

        long getSize();
    }
}
