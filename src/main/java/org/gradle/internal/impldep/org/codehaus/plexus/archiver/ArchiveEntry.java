//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributeUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoFileResource;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResourceWithAttributes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ArchiveEntry {
    public static final String ROLE = ArchiveEntry.class.getName();
    public static final int FILE = 1;
    public static final int DIRECTORY = 2;
    private PlexusIoResource resource;
    private String name;
    private int type;
    private int mode;
    private PlexusIoResourceAttributes attributes;

    private ArchiveEntry(String name, PlexusIoResource resource, int type, int mode) {
        this.name = name;
        this.resource = resource;
        this.attributes = resource instanceof PlexusIoResourceWithAttributes ? ((PlexusIoResourceWithAttributes)resource).getAttributes() : null;
        this.type = type;
        int permissions = mode;
        if (mode == -1 && this.attributes == null) {
            permissions = resource.isFile() ? '膤' : 16877;
        }

        this.mode = permissions == -1 ? permissions : permissions & 4095 | (type == 1 ? '耀' : 16384);
    }

    public String getName() {
        return this.name;
    }

    /** @deprecated */
    public File getFile() {
        return this.resource instanceof PlexusIoFileResource ? ((PlexusIoFileResource)this.resource).getFile() : null;
    }

    public InputStream getInputStream() throws IOException {
        return this.resource.getContents();
    }

    public int getType() {
        return this.type;
    }

    public int getMode() {
        if (this.mode != -1) {
            return this.mode;
        } else {
            return this.attributes != null && this.attributes.getOctalMode() > -1 ? this.attributes.getOctalMode() : (this.type == 1 ? '膤' : 16877) & 4095 | (this.type == 1 ? '耀' : 16384);
        }
    }

    public static ArchiveEntry createFileEntry(String target, PlexusIoResource resource, int permissions) throws ArchiverException {
        if (resource.isDirectory()) {
            throw new ArchiverException("Not a file: " + resource.getName());
        } else {
            return new ArchiveEntry(target, resource, 1, permissions);
        }
    }

    public static ArchiveEntry createFileEntry(String target, File file, int permissions) throws ArchiverException {
        if (!file.isFile()) {
            throw new ArchiverException("Not a file: " + file);
        } else {
            PlexusIoResourceAttributes attrs;
            try {
                attrs = PlexusIoResourceAttributeUtils.getFileAttributes(file);
            } catch (IOException var5) {
                throw new ArchiverException("Failed to read filesystem attributes for: " + file, var5);
            }

            PlexusIoFileResource res = PlexusIoFileResource.existingFile(file, attrs);
            return new ArchiveEntry(target, res, 1, permissions);
        }
    }

    public static ArchiveEntry createDirectoryEntry(String target, PlexusIoResource resource, int permissions) throws ArchiverException {
        if (!resource.isDirectory()) {
            throw new ArchiverException("Not a directory: " + resource.getName());
        } else {
            return new ArchiveEntry(target, resource, 2, permissions);
        }
    }

    public static ArchiveEntry createDirectoryEntry(String target, File file, int permissions) throws ArchiverException {
        if (!file.isDirectory()) {
            throw new ArchiverException("Not a directory: " + file);
        } else {
            PlexusIoResourceAttributes attrs;
            try {
                attrs = PlexusIoResourceAttributeUtils.getFileAttributes(file);
            } catch (IOException var5) {
                throw new ArchiverException("Failed to read filesystem attributes for: " + file, var5);
            }

            PlexusIoFileResource res = new PlexusIoFileResource(file, attrs);
            return new ArchiveEntry(target, res, 2, permissions);
        }
    }

    public static ArchiveEntry createEntry(String target, File file, int filePerm, int dirPerm) throws ArchiverException {
        if (file.isDirectory()) {
            return createDirectoryEntry(target, file, dirPerm);
        } else if (file.isFile()) {
            return createFileEntry(target, file, filePerm);
        } else {
            throw new ArchiverException("Neither a file nor a directory: " + file);
        }
    }

    public PlexusIoResourceAttributes getResourceAttributes() {
        return this.attributes;
    }

    public void setResourceAttributes(PlexusIoResourceAttributes attributes) {
        this.attributes = attributes;
    }

    public PlexusIoResource getResource() {
        return this.resource;
    }
}
