//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors.FileInfo;

import java.io.IOException;
import java.net.URL;

public interface PlexusIoResource extends FileInfo
{
    long UNKNOWN_RESOURCE_SIZE = -1L;
    long UNKNOWN_MODIFICATION_DATE = 0L;

    long getLastModified();

    boolean isExisting();

    long getSize();

    URL getURL() throws IOException;
}
