//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.filemappers;

public class PrefixFileMapper extends AbstractFileMapper {
    public static final String ROLE_HINT = "prefix";
    private String prefix;

    public PrefixFileMapper() {
    }

    public String getMappedFileName(String name) {
        String s = super.getMappedFileName(name);
        return getMappedFileName(this.prefix, s);
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public static String getMappedFileName(String prefix, String name) {
        return prefix != null && prefix.length() != 0 ? prefix + name : name;
    }
}
