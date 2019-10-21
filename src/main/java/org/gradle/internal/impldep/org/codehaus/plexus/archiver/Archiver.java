//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResourceCollection;
import org.gradle.internal.impldep.org.apache.maven.model.FileSet;

public interface Archiver {
    int DEFAULT_DIR_MODE = 16877;
    int DEFAULT_FILE_MODE = 33188;
    String ROLE = Archiver.class.getName();
    String DUPLICATES_ADD = "add";
    String DUPLICATES_PRESERVE = "preserve";
    String DUPLICATES_SKIP = "skip";
    String DUPLICATES_FAIL = "fail";
    Set DUPLICATES_VALID_BEHAVIORS = new HashSet() {
        private static final long serialVersionUID = 1L;

        {
            this.add("add");
            this.add("preserve");
            this.add("skip");
            this.add("fail");
        }
    };

    void createArchive() throws ArchiverException, IOException;

    void addDirectory(File var1) throws ArchiverException;

    void addDirectory(File var1, String var2) throws ArchiverException;

    void addDirectory(File var1, String[] var2, String[] var3) throws ArchiverException;

    void addDirectory(File var1, String var2, String[] var3, String[] var4) throws ArchiverException;

    void addFileSet(FileSet var1) throws ArchiverException;

    void addFile(File var1, String var2) throws ArchiverException;

    void addFile(File var1, String var2, int var3) throws ArchiverException;

    void addArchivedFileSet(File var1) throws ArchiverException;

    void addArchivedFileSet(File var1, String var2) throws ArchiverException;

    void addArchivedFileSet(File var1, String[] var2, String[] var3) throws ArchiverException;

    void addArchivedFileSet(File var1, String var2, String[] var3, String[] var4) throws ArchiverException;

    void addArchivedFileSet(ArchivedFileSet var1) throws ArchiverException;

    void addResource(PlexusIoResource var1, String var2, int var3) throws ArchiverException;

    void addResources(PlexusIoResourceCollection var1) throws ArchiverException;

    File getDestFile();

    void setDestFile(File var1);

    void setFileMode(int var1);

    int getFileMode();

    int getOverrideFileMode();

    void setDefaultFileMode(int var1);

    int getDefaultFileMode();

    void setDirectoryMode(int var1);

    int getDirectoryMode();

    int getOverrideDirectoryMode();

    void setDefaultDirectoryMode(int var1);

    int getDefaultDirectoryMode();

    boolean getIncludeEmptyDirs();

    void setIncludeEmptyDirs(boolean var1);

    void setDotFileDirectory(File var1);

    ResourceIterator getResources() throws ArchiverException;

    /** @deprecated */
    Map getFiles();

    boolean isForced();

    void setForced(boolean var1);

    boolean isSupportingForced();

    String getDuplicateBehavior();

    void setDuplicateBehavior(String var1);

    void setUseJvmChmod(boolean var1);

    boolean isUseJvmChmod();

    boolean isIgnorePermissions();

    void setIgnorePermissions(boolean var1);
}
