//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes;

public class SimpleResourceAttributes implements PlexusIoResourceAttributes {
    private Integer gid;
    private Integer uid;
    private String userName;
    private String groupName;
    private int mode;

    public SimpleResourceAttributes(Integer uid, String userName, Integer gid, String groupName, int mode) {
        this.uid = uid;
        this.userName = userName;
        this.gid = gid;
        this.groupName = groupName;
        this.mode = mode;
    }

    public SimpleResourceAttributes() {
    }

    public int getOctalMode() {
        return this.mode;
    }

    public Integer getGroupId() {
        return this.gid;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public Integer getUserId() {
        return this.uid;
    }

    public String getUserName() {
        return this.userName;
    }

    public boolean isGroupExecutable() {
        return PlexusIoResourceAttributeUtils.isGroupExecutableInOctal(this.mode);
    }

    public boolean isGroupReadable() {
        return PlexusIoResourceAttributeUtils.isGroupReadableInOctal(this.mode);
    }

    public boolean isGroupWritable() {
        return PlexusIoResourceAttributeUtils.isGroupWritableInOctal(this.mode);
    }

    public boolean isOwnerExecutable() {
        return PlexusIoResourceAttributeUtils.isOwnerExecutableInOctal(this.mode);
    }

    public boolean isOwnerReadable() {
        return PlexusIoResourceAttributeUtils.isOwnerReadableInOctal(this.mode);
    }

    public boolean isOwnerWritable() {
        return PlexusIoResourceAttributeUtils.isOwnerWritableInOctal(this.mode);
    }

    public boolean isWorldExecutable() {
        return PlexusIoResourceAttributeUtils.isWorldExecutableInOctal(this.mode);
    }

    public boolean isWorldReadable() {
        return PlexusIoResourceAttributeUtils.isWorldReadableInOctal(this.mode);
    }

    public boolean isWorldWritable() {
        return PlexusIoResourceAttributeUtils.isWorldWritableInOctal(this.mode);
    }

    public String getOctalModeString() {
        return Integer.toString(this.mode, 8);
    }

    public PlexusIoResourceAttributes setOctalMode(int mode) {
        this.mode = mode;
        return this;
    }

    public PlexusIoResourceAttributes setGroupExecutable(boolean flag) {
        this.set(8, flag);
        return this;
    }

    public PlexusIoResourceAttributes setGroupId(Integer gid) {
        this.gid = gid;
        return this;
    }

    public PlexusIoResourceAttributes setGroupName(String name) {
        this.groupName = name;
        return this;
    }

    public PlexusIoResourceAttributes setGroupReadable(boolean flag) {
        this.set(32, flag);
        return this;
    }

    public PlexusIoResourceAttributes setGroupWritable(boolean flag) {
        this.set(16, flag);
        return this;
    }

    public PlexusIoResourceAttributes setOwnerExecutable(boolean flag) {
        this.set(64, flag);
        return this;
    }

    public PlexusIoResourceAttributes setOwnerReadable(boolean flag) {
        this.set(256, flag);
        return this;
    }

    public PlexusIoResourceAttributes setOwnerWritable(boolean flag) {
        this.set(128, flag);
        return this;
    }

    public PlexusIoResourceAttributes setUserId(Integer uid) {
        this.uid = uid;
        return this;
    }

    public PlexusIoResourceAttributes setUserName(String name) {
        this.userName = name;
        return this;
    }

    public PlexusIoResourceAttributes setWorldExecutable(boolean flag) {
        this.set(1, flag);
        return this;
    }

    public PlexusIoResourceAttributes setWorldReadable(boolean flag) {
        this.set(4, flag);
        return this;
    }

    public PlexusIoResourceAttributes setWorldWritable(boolean flag) {
        this.set(2, flag);
        return this;
    }

    private void set(int bit, boolean enabled) {
        if (enabled) {
            this.mode |= bit;
        } else {
            this.mode &= ~bit;
        }

    }

    public PlexusIoResourceAttributes setOctalModeString(String mode) {
        this.setOctalMode(Integer.parseInt(mode, 8));
        return this;
    }

    public String toString() {
        return String.format("\nResource Attributes:\n------------------------------\nuser: %s\ngroup: %s\nuid: %d\ngid: %d\nmode: %06o", this.userName == null ? "" : this.userName, this.groupName == null ? "" : this.groupName, this.uid != null ? this.uid : 0, this.gid != null ? this.gid : 0, this.mode);
    }
}
