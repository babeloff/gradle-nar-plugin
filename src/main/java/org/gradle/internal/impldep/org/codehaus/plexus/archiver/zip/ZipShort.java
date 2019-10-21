//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip;

public final class ZipShort implements Cloneable {
    private int value;

    public ZipShort(int value) {
        this.value = value;
    }

    public ZipShort(byte[] bytes) {
        this(bytes, 0);
    }

    public ZipShort(byte[] bytes, int offset) {
        this.value = convert(bytes, offset);
    }

    public byte[] getBytes() {
        return bytes(this.value);
    }

    public int getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if (o != null && o instanceof ZipShort) {
            return this.value == ((ZipShort)o).getValue();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.value;
    }

    static int convert(byte[] bytes) {
        return convert(bytes, 0);
    }

    static int convert(byte[] bytes, int offset) {
        int value = bytes[offset + 1] << 8 & '\uff00';
        value += bytes[offset] & 255;
        return value;
    }

    static byte[] bytes(int value) {
        return bytes(value, new byte[2]);
    }

    static byte[] bytes(int value, byte[] result) {
        result[0] = (byte)(value & 255);
        result[1] = (byte)((value & '\uff00') >> 8);
        return result;
    }
}
