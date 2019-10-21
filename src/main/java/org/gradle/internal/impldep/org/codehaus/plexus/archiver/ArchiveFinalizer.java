//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import java.util.List;

public interface ArchiveFinalizer {
    void finalizeArchiveCreation(Archiver var1) throws ArchiverException;

    void finalizeArchiveExtraction(UnArchiver var1) throws ArchiverException;

    List getVirtualFiles();
}
