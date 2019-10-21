//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import java.util.Iterator;

public interface ResourceIterator extends Iterator<ArchiveEntry> {
    boolean hasNext();

    ArchiveEntry next();
}
