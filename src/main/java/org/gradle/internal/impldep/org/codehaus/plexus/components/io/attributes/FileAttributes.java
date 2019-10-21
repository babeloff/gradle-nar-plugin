//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes;

import java.util.Arrays;

public class FileAttributes implements PlexusIoResourceAttributes {
    protected static final char VALUE_DISABLED_MODE = '-';
    protected static final char VALUE_WRITABLE_MODE = 'w';
    protected static final char VALUE_READABLE_MODE = 'r';
    protected static final char VALUE_EXECUTABLE_MODE = 'x';
    protected static final int INDEX_WORLD_EXECUTE = 9;
    protected static final int INDEX_WORLD_WRITE = 8;
    protected static final int INDEX_WORLD_READ = 7;
    protected static final int INDEX_GROUP_EXECUTE = 6;
    protected static final int INDEX_GROUP_WRITE = 5;
    protected static final int INDEX_GROUP_READ = 4;
    protected static final int INDEX_OWNER_EXECUTE = 3;
    protected static final int INDEX_OWNER_WRITE = 2;
    protected static final int INDEX_OWNER_READ = 1;
    private Integer groupId;
    private String groupName;
    private Integer userId;
    private String userName;
    private char[] mode;

    public FileAttributes(int userId, String userName, int groupId, String groupName, char[] mode) {
        this.groupId = new Integer(groupId);
        this.groupName = groupName;
        this.userId = new Integer(userId);
        this.userName = userName;
        this.setLsModeParts(mode);
    }

    public FileAttributes() {
        this.mode = new char[10];
        Arrays.fill(this.mode, '-');
    }

    protected char[] getLsModeParts() {
        return this.mode;
    }

    protected void setLsModeParts(char[] mode) {
        if (mode.length < 10) {
            this.mode = new char[10];
            System.arraycopy(mode, 0, this.mode, 0, mode.length);

            for(int i = mode.length; i < 10; ++i) {
                this.mode[i] = '-';
            }
        } else {
            this.mode = mode;
        }

    }

    public Integer getGroupId() {
        return this.groupId;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public boolean isGroupExecutable() {
        return this.checkFlag('-', 6);
    }

    private boolean checkFlag(char disabledValue, int idx) {
        return this.mode == null ? false : this.mode[idx] != disabledValue;
    }

    public boolean isGroupReadable() {
        return this.checkFlag('-', 4);
    }

    public boolean isGroupWritable() {
        return this.checkFlag('-', 5);
    }

    public boolean isOwnerExecutable() {
        return this.checkFlag('-', 3);
    }

    public boolean isOwnerReadable() {
        return this.checkFlag('-', 1);
    }

    public boolean isOwnerWritable() {
        return this.checkFlag('-', 2);
    }

    public boolean isWorldExecutable() {
        return this.checkFlag('-', 9);
    }

    public boolean isWorldReadable() {
        return this.checkFlag('-', 7);
    }

    public boolean isWorldWritable() {
        return this.checkFlag('-', 8);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nFile Attributes:\n------------------------------\nuser: ");
        sb.append(this.userName == null ? "" : this.userName);
        sb.append("\ngroup: ");
        sb.append(this.groupName == null ? "" : this.groupName);
        sb.append("\nuid: ");
        sb.append(this.userId != null ? this.userId.toString() : "");
        sb.append("\ngid: ");
        sb.append(this.groupId != null ? this.groupId.toString() : "");
        sb.append("\nmode: ");
        sb.append(this.mode == null ? "" : String.valueOf(this.mode));
        return sb.toString();
    }

    public int getOctalMode() {
        int result = 0;
        if (this.isOwnerReadable()) {
            result |= 256;
        }

        if (this.isOwnerWritable()) {
            result |= 128;
        }

        if (this.isOwnerExecutable()) {
            result |= 64;
        }

        if (this.isGroupReadable()) {
            result |= 32;
        }

        if (this.isGroupWritable()) {
            result |= 16;
        }

        if (this.isGroupExecutable()) {
            result |= 8;
        }

        if (this.isWorldReadable()) {
            result |= 4;
        }

        if (this.isWorldWritable()) {
            result |= 2;
        }

        if (this.isWorldExecutable()) {
            result |= 1;
        }

        return result;
    }

    public String getOctalModeString() {
        return Integer.toString(this.getOctalMode(), 8);
    }

    public PlexusIoResourceAttributes setGroupExecutable(boolean flag) {
        this.setMode((char)(flag ? 'x' : '-'), 6);
        return this;
    }

    public PlexusIoResourceAttributes setGroupId(Integer gid) {
        this.groupId = new Integer(gid);
        return this;
    }

    public PlexusIoResourceAttributes setGroupName(String name) {
        this.groupName = name;
        return this;
    }

    public PlexusIoResourceAttributes setGroupReadable(boolean flag) {
        this.setMode((char)(flag ? 'r' : '-'), 4);
        return this;
    }

    public PlexusIoResourceAttributes setGroupWritable(boolean flag) {
        this.setMode((char)(flag ? 'w' : '-'), 5);
        return this;
    }

    public PlexusIoResourceAttributes setOwnerExecutable(boolean flag) {
        this.setMode((char)(flag ? 'x' : '-'), 3);
        return this;
    }

    public PlexusIoResourceAttributes setOwnerReadable(boolean flag) {
        this.setMode((char)(flag ? 'r' : '-'), 1);
        return this;
    }

    public PlexusIoResourceAttributes setOwnerWritable(boolean flag) {
        this.setMode((char)(flag ? 'w' : '-'), 2);
        return this;
    }

    public PlexusIoResourceAttributes setUserId(Integer uid) {
        this.userId = new Integer(uid);
        return this;
    }

    public PlexusIoResourceAttributes setUserName(String name) {
        this.userName = name;
        return this;
    }

    public PlexusIoResourceAttributes setWorldExecutable(boolean flag) {
        this.setMode((char)(flag ? 'x' : '-'), 9);
        return this;
    }

    public PlexusIoResourceAttributes setWorldReadable(boolean flag) {
        this.setMode((char)(flag ? 'r' : '-'), 7);
        return this;
    }

    public PlexusIoResourceAttributes setWorldWritable(boolean flag) {
        this.setMode((char)(flag ? 'w' : '-'), 8);
        return this;
    }

    public PlexusIoResourceAttributes setLsModeline(String modeLine) {
        this.setLsModeParts(modeLine.toCharArray());
        return this;
    }

    private void setMode(char value, int modeIdx) {
        char[] mode = this.getLsModeParts();
        mode[modeIdx] = value;
        this.setLsModeParts(mode);
    }

    public PlexusIoResourceAttributes setOctalMode(int mode) {
        this.setGroupExecutable(PlexusIoResourceAttributeUtils.isGroupExecutableInOctal(mode));
        this.setGroupReadable(PlexusIoResourceAttributeUtils.isGroupReadableInOctal(mode));
        this.setGroupWritable(PlexusIoResourceAttributeUtils.isGroupWritableInOctal(mode));
        this.setOwnerExecutable(PlexusIoResourceAttributeUtils.isOwnerExecutableInOctal(mode));
        this.setOwnerReadable(PlexusIoResourceAttributeUtils.isOwnerReadableInOctal(mode));
        this.setOwnerWritable(PlexusIoResourceAttributeUtils.isOwnerWritableInOctal(mode));
        this.setWorldExecutable(PlexusIoResourceAttributeUtils.isWorldExecutableInOctal(mode));
        this.setWorldReadable(PlexusIoResourceAttributeUtils.isWorldReadableInOctal(mode));
        this.setWorldWritable(PlexusIoResourceAttributeUtils.isWorldWritableInOctal(mode));
        return this;
    }

    public PlexusIoResourceAttributes setOctalModeString(String mode) {
        this.setOctalMode(Integer.parseInt(mode, 8));
        return this;
    }
}
