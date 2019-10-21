//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes;

public interface PlexusIoResourceAttributes {
    boolean isOwnerReadable();

    boolean isOwnerWritable();

    boolean isOwnerExecutable();

    boolean isGroupReadable();

    boolean isGroupWritable();

    boolean isGroupExecutable();

    boolean isWorldReadable();

    boolean isWorldWritable();

    boolean isWorldExecutable();

    Integer getUserId();

    Integer getGroupId();

    String getUserName();

    String getGroupName();

    int getOctalMode();

    String getOctalModeString();

    PlexusIoResourceAttributes setOwnerReadable(boolean var1);

    PlexusIoResourceAttributes setOwnerWritable(boolean var1);

    PlexusIoResourceAttributes setOwnerExecutable(boolean var1);

    PlexusIoResourceAttributes setGroupReadable(boolean var1);

    PlexusIoResourceAttributes setGroupWritable(boolean var1);

    PlexusIoResourceAttributes setGroupExecutable(boolean var1);

    PlexusIoResourceAttributes setWorldReadable(boolean var1);

    PlexusIoResourceAttributes setWorldWritable(boolean var1);

    PlexusIoResourceAttributes setWorldExecutable(boolean var1);

    PlexusIoResourceAttributes setUserId(Integer var1);

    PlexusIoResourceAttributes setGroupId(Integer var1);

    PlexusIoResourceAttributes setUserName(String var1);

    PlexusIoResourceAttributes setGroupName(String var1);

    PlexusIoResourceAttributes setOctalMode(int var1);

    PlexusIoResourceAttributes setOctalModeString(String var1);
}
