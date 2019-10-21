//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PlexusIoFileResource extends AbstractPlexusIoResourceWithAttributes implements PlexusIoResourceWithAttributes {
    private File file;

    public PlexusIoFileResource() {
    }

    public PlexusIoFileResource(File file) {
        this(file, getName(file));
    }

    public PlexusIoFileResource(File file, PlexusIoResourceAttributes attrs) {
        this(file, getName(file), attrs);
    }

    public PlexusIoFileResource(File file, String name) {
        this(file, name, (PlexusIoResourceAttributes)null, true);
    }

    public PlexusIoFileResource(File file, String name, PlexusIoResourceAttributes attrs) {
        this(file, name, attrs, true);
    }

    protected PlexusIoFileResource(File file, String name, PlexusIoResourceAttributes attrs, boolean setPhysicalFileAttribute) {
        this.setName(name);
        if (attrs != null) {
            this.setAttributes(attrs);
        }

        this.setFile(file, setPhysicalFileAttribute);
    }

    private static String getName(File file) {
        return file.getPath().replace('\\', '/');
    }

    public static PlexusIoFileResource readFromDisk(File file, String name, PlexusIoResourceAttributes attrs) {
        return new PlexusIoFileResource(file, name, attrs, false);
    }

    public static PlexusIoFileResource existingFile(File file, PlexusIoResourceAttributes attrs) {
        return new PlexusIoFileResource(file, getName(file), attrs, false);
    }

    private void setFile(File file, boolean setPhysicalFileAttribute) {
        this.file = file;
        if (setPhysicalFileAttribute) {
            this.setLastModified(file.lastModified());
        }

        this.setSize(file.length());
        this.setFile(file.isFile());
        this.setDirectory(file.isDirectory());
        this.setExisting(file.exists());
    }

    public File getFile() {
        return this.file;
    }

    public InputStream getContents() throws IOException {
        return new FileInputStream(this.getFile());
    }

    public URL getURL() throws IOException {
        return this.getFile().toURI().toURL();
    }

    public long getLastModified() {
        return this.getFile().lastModified();
    }

    public long getSize() {
        return this.getFile().length();
    }

    public boolean isDirectory() {
        return this.getFile().isDirectory();
    }

    public boolean isExisting() {
        return this.getFile().exists();
    }

    public boolean isFile() {
        return this.getFile().isFile();
    }

    public void setDirectory(boolean isDirectory) {
    }

    public void setExisting(boolean isExisting) {
    }

    public void setFile(boolean isFile) {
    }

    public void setLastModified(long lastModified) {
        this.file.setLastModified(lastModified);
    }

    public void setSize(long size) {
    }
}
