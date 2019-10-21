//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.proxy;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.AbstractPlexusIoResourceWithAttributes;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResourceWithAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PlexusIoProxyResourceWithAttributes extends AbstractPlexusIoResourceWithAttributes
{
    private final PlexusIoResource src;

    public PlexusIoProxyResourceWithAttributes(PlexusIoResourceWithAttributes plexusIoResource) {
        this.src = plexusIoResource;
        this.setName(this.src.getName());
        this.setAttributes(plexusIoResource.getAttributes());
    }

    public PlexusIoProxyResourceWithAttributes(PlexusIoResource plexusIoResource, PlexusIoResourceAttributes attrs) {
        this.src = plexusIoResource;
        this.setName(this.src.getName());
        this.setAttributes(attrs);
    }

    public long getLastModified() {
        return this.src.getLastModified();
    }

    public long getSize() {
        return this.src.getSize();
    }

    public boolean isDirectory() {
        return this.src.isDirectory();
    }

    public boolean isExisting() {
        return this.src.isExisting();
    }

    public boolean isFile() {
        return this.src.isFile();
    }

    public URL getURL() throws IOException {
        return this.src.getURL();
    }

    public InputStream getContents() throws IOException {
        return this.src.getContents();
    }
}
