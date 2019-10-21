//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources;

public abstract class AbstractPlexusIoResource implements PlexusIoResource {
    private String name;
    private long lastModified;
    private long size;
    private boolean isFile;
    private boolean isDirectory;
    private boolean isExisting;

    public AbstractPlexusIoResource() {
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return this.size;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public void setExisting(boolean isExisting) {
        this.isExisting = isExisting;
    }

    public boolean isExisting() {
        return this.isExisting;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }

    public boolean isFile() {
        return this.isFile;
    }
}
