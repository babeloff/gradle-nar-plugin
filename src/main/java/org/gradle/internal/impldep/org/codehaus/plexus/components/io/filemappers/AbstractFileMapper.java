//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.filemappers;

public abstract class AbstractFileMapper implements FileMapper {
    public AbstractFileMapper() {
    }

    public String getMappedFileName(String pName) {
        if (pName != null && pName.length() != 0) {
            return pName;
        } else {
            throw new IllegalArgumentException("The source name must not be null.");
        }
    }
}
