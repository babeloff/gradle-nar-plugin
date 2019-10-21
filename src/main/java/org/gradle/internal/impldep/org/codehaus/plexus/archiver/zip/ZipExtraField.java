//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip;

import java.util.zip.ZipException;

public interface ZipExtraField {
    ZipShort getHeaderId();

    ZipShort getLocalFileDataLength();

    ZipShort getCentralDirectoryLength();

    byte[] getLocalFileDataData();

    byte[] getCentralDirectoryData();

    void parseFromLocalFileData(byte[] var1, int var2, int var3) throws ZipException;
}
