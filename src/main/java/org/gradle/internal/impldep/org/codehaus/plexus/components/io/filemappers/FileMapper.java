//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.filemappers;

public interface FileMapper {
    String ROLE = FileMapper.class.getName();
    String DEFAULT_ROLE_HINT = "default";

    String getMappedFileName(String var1);
}
