//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources;

import java.io.IOException;
import java.util.Iterator;

public interface PlexusIoResourceCollection {
    String ROLE = PlexusIoResourceCollection.class.getName();
    String DEFAULT_ROLE_HINT = "default";

    Iterator<PlexusIoResource> getResources() throws IOException;

    String getName(PlexusIoResource var1) throws IOException;

    long getLastModified() throws IOException;
}
