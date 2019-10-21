//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip;

import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiveFile.Entry;

import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.zip.ZipException;

public class ZipEntry extends java.util.zip.ZipEntry implements Cloneable, Entry
{
    private static final int PLATFORM_UNIX = 3;
    private static final int PLATFORM_FAT = 0;
    private int internalAttributes;
    private int platform;
    private long externalAttributes;
    private Vector<ZipExtraField> extraFields;
    private String name;

    public ZipEntry(String name) {
        super(name);
        this.internalAttributes = 0;
        this.platform = 0;
        this.externalAttributes = 0L;
        this.extraFields = new Vector();
        this.name = null;
    }

    public ZipEntry(java.util.zip.ZipEntry entry) throws ZipException {
        super(entry.getName());
        this.internalAttributes = 0;
        this.platform = 0;
        this.externalAttributes = 0L;
        this.extraFields = new Vector();
        this.name = null;
        this.setComment(entry.getComment());
        this.setMethod(entry.getMethod());
        this.setTime(entry.getTime());
        long size = entry.getSize();
        if (size > 0L) {
            this.setSize(size);
        }

        long cSize = entry.getCompressedSize();
        if (cSize > 0L) {
            this.setComprSize(cSize);
        }

        long crc = entry.getCrc();
        if (crc > 0L) {
            this.setCrc(crc);
        }

        byte[] extra = entry.getExtra();
        if (extra != null) {
            this.setExtraFields(ExtraFieldUtils.parse(extra));
        } else {
            this.setExtra();
        }

    }

    public ZipEntry(ZipEntry entry) throws ZipException {
        this((java.util.zip.ZipEntry)entry);
        this.setInternalAttributes(entry.getInternalAttributes());
        this.setExternalAttributes(entry.getExternalAttributes());
        this.setExtraFields(entry.getExtraFields());
    }

    protected ZipEntry() {
        super("");
        this.internalAttributes = 0;
        this.platform = 0;
        this.externalAttributes = 0L;
        this.extraFields = new Vector();
        this.name = null;
    }

    public Object clone() {
        try {
            ZipEntry e = (ZipEntry)super.clone();
            e.setName(this.getName());
            e.setComment(this.getComment());
            e.setMethod(this.getMethod());
            e.setTime(this.getTime());
            long size = this.getSize();
            if (size > 0L) {
                e.setSize(size);
            }

            long cSize = this.getCompressedSize();
            if (cSize > 0L) {
                e.setComprSize(cSize);
            }

            long crc = this.getCrc();
            if (crc > 0L) {
                e.setCrc(crc);
            }

            e.extraFields = (Vector)this.extraFields.clone();
            e.setInternalAttributes(this.getInternalAttributes());
            e.setExternalAttributes(this.getExternalAttributes());
            e.setExtraFields(this.getExtraFields());
            return e;
        } catch (Throwable var8) {
            return null;
        }
    }

    public int getInternalAttributes() {
        return this.internalAttributes;
    }

    public void setInternalAttributes(int value) {
        this.internalAttributes = value;
    }

    public long getExternalAttributes() {
        return this.externalAttributes;
    }

    public void setExternalAttributes(long value) {
        this.externalAttributes = value;
    }

    public void setUnixMode(int mode) {
        this.setExternalAttributes((long)(mode << 16 | ((mode & 128) == 0 ? 1 : 0) | (this.isDirectory() ? 16 : 0)));
        this.platform = 3;
    }

    public int getUnixMode() {
        return (int)(this.getExternalAttributes() >> 16 & 65535L);
    }

    public int getPlatform() {
        return this.platform;
    }

    protected void setPlatform(int platform) {
        this.platform = platform;
    }

    public void setExtraFields(ZipExtraField[] fields) {
        this.extraFields.removeAllElements();
        ZipExtraField[] arr$ = fields;
        int len$ = fields.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            ZipExtraField field = arr$[i$];
            this.extraFields.addElement(field);
        }

        this.setExtra();
    }

    public ZipExtraField[] getExtraFields() {
        ZipExtraField[] result = new ZipExtraField[this.extraFields.size()];
        this.extraFields.copyInto(result);
        return result;
    }

    public void addExtraField(ZipExtraField ze) {
        ZipShort type = ze.getHeaderId();
        boolean done = false;

        for(int i = 0; !done && i < this.extraFields.size(); ++i) {
            if (((ZipExtraField)this.extraFields.elementAt(i)).getHeaderId().equals(type)) {
                this.extraFields.setElementAt(ze, i);
                done = true;
            }
        }

        if (!done) {
            this.extraFields.addElement(ze);
        }

        this.setExtra();
    }

    public void removeExtraField(ZipShort type) {
        boolean done = false;

        for(int i = 0; !done && i < this.extraFields.size(); ++i) {
            if (((ZipExtraField)this.extraFields.elementAt(i)).getHeaderId().equals(type)) {
                this.extraFields.removeElementAt(i);
                done = true;
            }
        }

        if (!done) {
            throw new NoSuchElementException();
        } else {
            this.setExtra();
        }
    }

    public void setExtra(byte[] extra) throws RuntimeException {
        try {
            this.setExtraFields(ExtraFieldUtils.parse(extra));
        } catch (Exception var3) {
            throw new RuntimeException(var3.getMessage());
        }
    }

    protected void setExtra() {
        super.setExtra(ExtraFieldUtils.mergeLocalFileDataData(this.getExtraFields()));
    }

    public byte[] getLocalFileDataExtra() {
        byte[] extra = this.getExtra();
        return extra != null ? extra : new byte[0];
    }

    public byte[] getCentralDirectoryExtra() {
        return ExtraFieldUtils.mergeCentralDirectoryData(this.getExtraFields());
    }

    public void setComprSize(long size) {
        this.setCompressedSize(size);
    }

    public String getName() {
        return this.name == null ? super.getName() : this.name;
    }

    public boolean isDirectory() {
        return this.getName().endsWith("/");
    }

    protected void setName(String name) {
        this.name = name;
    }

    public long getLastModificationTime() {
        return this.getTime();
    }
}
