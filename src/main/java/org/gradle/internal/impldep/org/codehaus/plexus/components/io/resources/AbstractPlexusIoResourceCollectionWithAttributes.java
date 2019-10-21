//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources;


import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.Logger;

public abstract class AbstractPlexusIoResourceCollectionWithAttributes extends AbstractPlexusIoResourceCollection implements PlexusIOResourceCollectionWithAttributes {
    private PlexusIoResourceAttributes defaultFileAttributes;
    private PlexusIoResourceAttributes defaultDirAttributes;
    private PlexusIoResourceAttributes overrideFileAttributes;
    private PlexusIoResourceAttributes overrideDirAttributes;

    protected AbstractPlexusIoResourceCollectionWithAttributes() {
    }

    protected AbstractPlexusIoResourceCollectionWithAttributes(Logger logger) {
        super(logger);
    }

    protected PlexusIoResourceAttributes getDefaultFileAttributes() {
        return this.defaultFileAttributes;
    }

    protected void setDefaultFileAttributes(PlexusIoResourceAttributes defaultFileAttributes) {
        this.defaultFileAttributes = defaultFileAttributes;
    }

    protected PlexusIoResourceAttributes getDefaultDirAttributes() {
        return this.defaultDirAttributes;
    }

    protected void setDefaultDirAttributes(PlexusIoResourceAttributes defaultDirAttributes) {
        this.defaultDirAttributes = defaultDirAttributes;
    }

    protected PlexusIoResourceAttributes getOverrideFileAttributes() {
        return this.overrideFileAttributes;
    }

    protected void setOverrideFileAttributes(PlexusIoResourceAttributes overrideFileAttributes) {
        this.overrideFileAttributes = overrideFileAttributes;
    }

    protected PlexusIoResourceAttributes getOverrideDirAttributes() {
        return this.overrideDirAttributes;
    }

    protected void setOverrideDirAttributes(PlexusIoResourceAttributes overrideDirAttributes) {
        this.overrideDirAttributes = overrideDirAttributes;
    }
}
