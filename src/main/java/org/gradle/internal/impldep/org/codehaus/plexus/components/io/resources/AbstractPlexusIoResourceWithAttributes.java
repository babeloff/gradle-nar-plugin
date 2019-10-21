//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources;


import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;

public abstract class AbstractPlexusIoResourceWithAttributes extends AbstractPlexusIoResource implements PlexusIoResourceWithAttributes {
    private PlexusIoResourceAttributes attributes;

    public AbstractPlexusIoResourceWithAttributes() {
    }

    public PlexusIoResourceAttributes getAttributes() {
        return this.attributes;
    }

    public void setAttributes(PlexusIoResourceAttributes attributes) {
        this.attributes = attributes;
    }
}
