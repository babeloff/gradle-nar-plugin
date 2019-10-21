//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.manager;

import org.gradle.internal.impldep.org.codehaus.plexus.archiver.Archiver;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.UnArchiver;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResourceCollection;

import java.io.File;

public interface ArchiverManager {
    String ROLE = ArchiverManager.class.getName();

    Archiver getArchiver(String var1) throws NoSuchArchiverException;

    Archiver getArchiver(File var1) throws NoSuchArchiverException;

    UnArchiver getUnArchiver(String var1) throws NoSuchArchiverException;

    UnArchiver getUnArchiver(File var1) throws NoSuchArchiverException;

    PlexusIoResourceCollection getResourceCollection(File var1) throws NoSuchArchiverException;

    PlexusIoResourceCollection getResourceCollection(String var1) throws NoSuchArchiverException;
}
