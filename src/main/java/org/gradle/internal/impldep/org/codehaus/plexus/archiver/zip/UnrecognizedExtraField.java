//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip;

public class UnrecognizedExtraField implements ZipExtraField {
    private ZipShort headerId;
    private byte[] localData;
    private byte[] centralData;

    public UnrecognizedExtraField() {
    }

    public void setHeaderId(ZipShort headerId) {
        this.headerId = headerId;
    }

    public ZipShort getHeaderId() {
        return this.headerId;
    }

    public void setLocalFileDataData(byte[] data) {
        this.localData = data;
    }

    public ZipShort getLocalFileDataLength() {
        return new ZipShort(this.localData.length);
    }

    public byte[] getLocalFileDataData() {
        return this.localData;
    }

    public void setCentralDirectoryData(byte[] data) {
        this.centralData = data;
    }

    public ZipShort getCentralDirectoryLength() {
        return this.centralData != null ? new ZipShort(this.centralData.length) : this.getLocalFileDataLength();
    }

    public byte[] getCentralDirectoryData() {
        return this.centralData != null ? this.centralData : this.getLocalFileDataData();
    }

    public void parseFromLocalFileData(byte[] data, int offset, int length) {
        byte[] tmp = new byte[length];
        System.arraycopy(data, offset, tmp, 0, length);
        this.setLocalFileDataData(tmp);
    }
}
