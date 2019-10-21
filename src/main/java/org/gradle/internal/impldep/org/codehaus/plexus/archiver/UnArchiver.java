//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors.FileSelector;

import java.io.File;


public interface UnArchiver {
    String ROLE = UnArchiver.class.getName();

    void extract() throws ArchiverException;

    void extract(String var1, File var2) throws ArchiverException;

    File getDestDirectory();

    void setDestDirectory(File var1);

    File getDestFile();

    void setDestFile(File var1);

    File getSourceFile();

    void setSourceFile(File var1);

    void setOverwrite(boolean var1);

    void setFileSelectors(FileSelector[] var1);

    FileSelector[] getFileSelectors();

    void setUseJvmChmod(boolean var1);

    boolean isUseJvmChmod();

    boolean isIgnorePermissions();

    void setIgnorePermissions(boolean var1);
}
