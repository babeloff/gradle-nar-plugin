//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import java.io.InputStream;

/** @deprecated */
public interface ArchiveFileFilter {
    boolean include(InputStream var1, String var2) throws ArchiveFilterException;
}
