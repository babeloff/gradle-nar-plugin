//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip;

public final class ZipLong implements Cloneable {
    private long value;

    public ZipLong(long value) {
        this.value = value;
    }

    public ZipLong(byte[] bytes) {
        this(bytes, 0);
    }

    public ZipLong(byte[] bytes, int offset) {
        this.value = convert(bytes, offset);
    }

    public byte[] getBytes() {
        return bytes(this.value);
    }

    public long getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if (o != null && o instanceof ZipLong) {
            return this.value == ((ZipLong)o).getValue();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return (int)this.value;
    }

    static long convert(byte[] bytes) {
        return convert(bytes, 0);
    }

    static long convert(byte[] bytes, int offset) {
        long value = (long)(bytes[offset + 3] << 24) & 4278190080L;
        value += (long)(bytes[offset + 2] << 16 & 16711680);
        value += (long)(bytes[offset + 1] << 8 & '\uff00');
        value += (long)(bytes[offset] & 255);
        return value;
    }

    static byte[] bytes(long value) {
        return bytes(value, new byte[4]);
    }

    static byte[] bytes(long value, byte[] result) {
        result[0] = (byte)((int)(value & 255L));
        result[1] = (byte)((int)((value & 65280L) >> 8));
        result[2] = (byte)((int)((value & 16711680L) >> 16));
        result[3] = (byte)((int)((value & 4278190080L) >> 24));
        return result;
    }
}
