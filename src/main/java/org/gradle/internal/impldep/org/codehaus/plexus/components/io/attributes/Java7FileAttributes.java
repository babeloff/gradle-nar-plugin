//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;

public class Java7FileAttributes implements PlexusIoResourceAttributes {
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
    private int groupId = -1;
    private String groupName;
    private int userId = -1;
    private String userName;
    private char[] mode;

    public Java7FileAttributes(File file, Map<Integer, String> userCache, Map<Integer, String> groupCache) throws IOException {
        PosixFileAttributes posixFileAttributes = getPosixFileAttributes(file);
        Integer uid = (Integer)Files.readAttributes(file.toPath(), "unix:uid").get("uid");
        String userName = (String)userCache.get(uid);
        if (userName != null) {
            this.userName = userName;
        } else {
            this.userName = posixFileAttributes.owner().getName();
            userCache.put(uid, this.userName);
        }

        Integer gid = (Integer)Files.readAttributes(file.toPath(), "unix:gid").get("gid");
        String groupName = (String)groupCache.get(gid);
        if (groupName != null) {
            this.groupName = groupName;
        } else {
            this.groupName = posixFileAttributes.group().getName();
            groupCache.put(gid, this.groupName);
        }

        this.setLsModeParts(PosixFilePermissions.toString(posixFileAttributes.permissions()).toCharArray());
    }

    static PosixFileAttributes getPosixFileAttributes(File file) throws IOException {
        return (PosixFileAttributes)Files.readAttributes(file.toPath(), PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
    }

    protected char[] getLsModeParts() {
        return this.mode;
    }

    protected void setLsModeParts(char[] mode) {
        this.mode = new char[10];
        this.mode[0] = '-';
        System.arraycopy(mode, 0, this.mode, 1, mode.length);
    }

    public Integer getGroupId() {
        return this.groupId;
    }

    public boolean hasGroupId() {
        return false;
    }

    public boolean hasUserId() {
        return false;
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
        return this.mode != null && this.mode[idx] != disabledValue;
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
        sb.append(this.hasUserId() ? Integer.toString(this.userId) : "");
        sb.append("\ngid: ");
        sb.append(this.hasGroupId() ? Integer.toString(this.groupId) : "");
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
        this.groupId = gid;
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
        this.userId = uid;
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
