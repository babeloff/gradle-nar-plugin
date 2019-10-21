//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.util;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoFileResource;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.gradle.internal.impldep.org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceUtils {
    private ResourceUtils() {
    }

    public static boolean isUptodate(PlexusIoResource source, File destination) {
        return isUptodate(source, destination.lastModified());
    }

    public static boolean isUptodate(PlexusIoResource source, long destinationDate) {
        long s = source.getLastModified();
        if (s == 0L) {
            return false;
        } else if (destinationDate == 0L) {
            return false;
        } else {
            return destinationDate > s;
        }
    }

    public static boolean isUptodate(long sourceDate, long destinationDate) {
        if (sourceDate == 0L) {
            return false;
        } else if (destinationDate == 0L) {
            return false;
        } else {
            return destinationDate > sourceDate;
        }
    }

    public static void copyFile(PlexusIoResource in, File outFile) throws IOException {
        InputStream input = null;
        FileOutputStream output = null;

        try {
            input = in.getContents();
            output = new FileOutputStream(outFile);
            IOUtil.copy(input, output);
        } finally {
            IOUtil.close(input);
            IOUtil.close(output);
        }

    }

    public static boolean isSame(PlexusIoResource resource, File file) {
        if (resource instanceof PlexusIoFileResource) {
            File resourceFile = ((PlexusIoFileResource)resource).getFile();
            return file.equals(resourceFile);
        } else {
            return false;
        }
    }

    public static boolean isCanonicalizedSame(PlexusIoResource resource, File file) throws IOException {
        if (resource instanceof PlexusIoFileResource) {
            File resourceFile = ((PlexusIoFileResource)resource).getFile();
            return file.getCanonicalFile().equals(resourceFile.getCanonicalFile());
        } else {
            return false;
        }
    }
}
