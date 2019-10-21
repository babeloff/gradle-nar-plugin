//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip;

import java.util.zip.CRC32;
import java.util.zip.ZipException;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.UnixStat;

public class AsiExtraField implements ZipExtraField, UnixStat, Cloneable {
    private static final ZipShort HEADER_ID = new ZipShort(30062);
    private int mode = 0;
    private int uid = 0;
    private int gid = 0;
    private String link = "";
    private boolean dirFlag = false;
    private CRC32 crc = new CRC32();

    public AsiExtraField() {
    }

    public ZipShort getHeaderId() {
        return HEADER_ID;
    }

    public ZipShort getLocalFileDataLength() {
        return new ZipShort(14 + this.getLinkedFile().getBytes().length);
    }

    public ZipShort getCentralDirectoryLength() {
        return this.getLocalFileDataLength();
    }

    public byte[] getLocalFileDataData() {
        byte[] data = new byte[this.getLocalFileDataLength().getValue() - 4];
        System.arraycopy((new ZipShort(this.getMode())).getBytes(), 0, data, 0, 2);
        byte[] linkArray = this.getLinkedFile().getBytes();
        System.arraycopy((new ZipLong((long)linkArray.length)).getBytes(), 0, data, 2, 4);
        System.arraycopy((new ZipShort(this.getUserId())).getBytes(), 0, data, 6, 2);
        System.arraycopy((new ZipShort(this.getGroupId())).getBytes(), 0, data, 8, 2);
        System.arraycopy(linkArray, 0, data, 10, linkArray.length);
        this.crc.reset();
        this.crc.update(data);
        long checksum = this.crc.getValue();
        byte[] result = new byte[data.length + 4];
        System.arraycopy((new ZipLong(checksum)).getBytes(), 0, result, 0, 4);
        System.arraycopy(data, 0, result, 4, data.length);
        return result;
    }

    public byte[] getCentralDirectoryData() {
        return this.getLocalFileDataData();
    }

    public void setUserId(int uid) {
        this.uid = uid;
    }

    public int getUserId() {
        return this.uid;
    }

    public void setGroupId(int gid) {
        this.gid = gid;
    }

    public int getGroupId() {
        return this.gid;
    }

    public void setLinkedFile(String name) {
        this.link = name;
        this.mode = this.getMode(this.mode);
    }

    public String getLinkedFile() {
        return this.link;
    }

    public boolean isLink() {
        return this.getLinkedFile().length() != 0;
    }

    public void setMode(int mode) {
        this.mode = this.getMode(mode);
    }

    public int getMode() {
        return this.mode;
    }

    public void setDirectory(boolean dirFlag) {
        this.dirFlag = dirFlag;
        this.mode = this.getMode(this.mode);
    }

    public boolean isDirectory() {
        return this.dirFlag && !this.isLink();
    }

    public void parseFromLocalFileData(byte[] data, int offset, int length) throws ZipException {
        long givenChecksum = (new ZipLong(data, offset)).getValue();
        byte[] tmp = new byte[length - 4];
        System.arraycopy(data, offset + 4, tmp, 0, length - 4);
        this.crc.reset();
        this.crc.update(tmp);
        long realChecksum = this.crc.getValue();
        if (givenChecksum != realChecksum) {
            throw new ZipException("bad CRC checksum " + Long.toHexString(givenChecksum) + " instead of " + Long.toHexString(realChecksum));
        } else {
            int newMode = (new ZipShort(tmp, 0)).getValue();
            byte[] linkArray = new byte[(int)(new ZipLong(tmp, 2)).getValue()];
            this.uid = (new ZipShort(tmp, 6)).getValue();
            this.gid = (new ZipShort(tmp, 8)).getValue();
            if (linkArray.length == 0) {
                this.link = "";
            } else {
                System.arraycopy(tmp, 10, linkArray, 0, linkArray.length);
                this.link = new String(linkArray);
            }

            this.setDirectory((newMode & 16384) != 0);
            this.setMode(newMode);
        }
    }

    protected int getMode(int mode) {
        int type = '耀';
        if (this.isLink()) {
            type = 'ꀀ';
        } else if (this.isDirectory()) {
            type = 16384;
        }

        return type | mode & 4095;
    }
}
