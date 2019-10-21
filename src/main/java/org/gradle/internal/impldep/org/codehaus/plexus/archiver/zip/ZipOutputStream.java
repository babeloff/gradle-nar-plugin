//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

public class ZipOutputStream extends FilterOutputStream {
    private ZipEntry entry;
    private String comment = "";
    private int level = -1;
    private boolean hasCompressionLevelChanged = false;
    private int method = 8;
    private Vector entries = new Vector();
    private CRC32 crc = new CRC32();
    private long written = 0L;
    private long dataStart = 0L;
    private long localDataStart = 0L;
    private ZipLong cdOffset = new ZipLong(0L);
    private ZipLong cdLength = new ZipLong(0L);
    private static final byte[] ZERO = new byte[]{0, 0};
    private static final byte[] LZERO = new byte[]{0, 0, 0, 0};
    private Hashtable offsets = new Hashtable();
    private String encoding = null;
    protected Deflater def = new Deflater(-1, true);
    protected byte[] buf = new byte[512];
    private RandomAccessFile raf = null;
    private static final String UTF8 = "UTF-8";
    public static final int DEFLATED = 8;
    public static final int STORED = 0;
    final byte[] oneByte = new byte[1];
    protected static final ZipLong LFH_SIG = new ZipLong(67324752L);
    protected static final ZipLong DD_SIG = new ZipLong(134695760L);
    protected static final ZipLong CFH_SIG = new ZipLong(33639248L);
    protected static final ZipLong EOCD_SIG = new ZipLong(101010256L);
    static final byte[] Z20_bytes = ZipShort.bytes(20);
    static final byte[] Z8_bytes = ZipShort.bytes(8);
    static final byte[] Z10_bytes = ZipShort.bytes(10);
    static final byte[] Z2048_bytes = ZipShort.bytes(2048);
    static final byte[] Z2056_bytes = ZipShort.bytes(2056);
    static final byte[] LFH_SIG_bytes;
    static final byte[] DD_SIG_bytes;
    static final byte[] CFH_SIG_bytes;
    static final byte[] EOCD_SIG_bytes;
    private static final ZipLong DOS_TIME_MIN;
    final byte[] shortBuffer = new byte[2];
    final byte[] longBuffer = new byte[4];

    public ZipOutputStream(OutputStream out) {
        super(out);
    }

    public ZipOutputStream(File file) throws IOException {
        super((OutputStream)null);

        try {
            this.raf = new RandomAccessFile(file, "rw");
            this.raf.setLength(0L);
        } catch (IOException var5) {
            if (this.raf != null) {
                try {
                    this.raf.close();
                } catch (IOException var4) {
                }

                this.raf = null;
            }

            this.out = new FileOutputStream(file);
        }

    }

    public boolean isSeekable() {
        return this.raf != null;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void finish() throws IOException {
        this.closeEntry();
        this.cdOffset = new ZipLong(this.written);

        for(int i = 0; i < this.entries.size(); ++i) {
            this.writeCentralFileHeader((ZipEntry)this.entries.elementAt(i));
        }

        this.cdLength = new ZipLong(this.written - this.cdOffset.getValue());
        this.writeCentralDirectoryEnd();
        this.offsets.clear();
        this.entries.removeAllElements();
    }

    public void closeEntry() throws IOException {
        if (this.entry != null) {
            long realCrc = this.crc.getValue();
            this.crc.reset();
            long save;
            if (this.entry.getMethod() == 8) {
                this.def.finish();

                while(!this.def.finished()) {
                    this.deflate();
                }

                this.entry.setSize(this.def.getBytesRead());
                this.entry.setComprSize((long)this.def.getTotalOut());
                this.entry.setCrc(realCrc);
                this.def.reset();
                this.written += this.entry.getCompressedSize();
            } else if (this.raf == null) {
                if (this.entry.getCrc() != realCrc) {
                    throw new ZipException("bad CRC checksum for entry " + this.entry.getName() + ": " + Long.toHexString(this.entry.getCrc()) + " instead of " + Long.toHexString(realCrc));
                }

                if (this.entry.getSize() != this.written - this.dataStart) {
                    throw new ZipException("bad size for entry " + this.entry.getName() + ": " + this.entry.getSize() + " instead of " + (this.written - this.dataStart));
                }
            } else {
                save = this.written - this.dataStart;
                this.entry.setSize(save);
                this.entry.setComprSize(save);
                this.entry.setCrc(realCrc);
            }

            if (this.raf != null) {
                save = this.raf.getFilePointer();
                this.raf.seek(this.localDataStart);
                this.writeOut(this.longBytes(this.entry.getCrc()));
                this.writeOut(this.longBytes(this.entry.getCompressedSize()));
                this.writeOut(this.longBytes(this.entry.getSize()));
                this.raf.seek(save);
            }

            this.writeDataDescriptor(this.entry);
            this.entry = null;
        }
    }

    public void putNextEntry(ZipEntry ze) throws IOException {
        this.closeEntry();
        this.entry = ze;
        this.entries.addElement(this.entry);
        if (this.entry.getMethod() == -1) {
            this.entry.setMethod(this.method);
        }

        if (this.entry.getTime() == -1L) {
            this.entry.setTime(System.currentTimeMillis());
        }

        if (this.entry.getMethod() == 0 && this.raf == null) {
            if (this.entry.getSize() == -1L) {
                throw new ZipException("uncompressed size is required for STORED method when not writing to a file");
            }

            if (this.entry.getCrc() == -1L) {
                throw new ZipException("crc checksum is required for STORED method when not writing to a file");
            }

            this.entry.setComprSize(this.entry.getSize());
        }

        if (this.entry.getMethod() == 8 && this.hasCompressionLevelChanged) {
            this.def.setLevel(this.level);
            this.hasCompressionLevelChanged = false;
        }

        this.writeLocalFileHeader(this.entry);
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setLevel(int level) {
        this.hasCompressionLevelChanged = this.level != level;
        this.level = level;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public void write(byte[] b, int offset, int length) throws IOException {
        if (this.entry.getMethod() == 8) {
            if (length > 0 && !this.def.finished()) {
                this.def.setInput(b, offset, length);

                while(!this.def.needsInput()) {
                    this.deflate();
                }
            }
        } else {
            this.writeOut(b, offset, length);
            this.written += (long)length;
        }

        this.crc.update(b, offset, length);
    }

    public void write(int b) throws IOException {
        this.oneByte[0] = (byte)(b & 255);
        this.write(this.oneByte, 0, 1);
    }

    public void close() throws IOException {
        this.finish();
        if (this.raf != null) {
            this.raf.close();
        }

        if (this.out != null) {
            this.out.close();
        }

    }

    public void flush() throws IOException {
        if (this.out != null) {
            this.out.flush();
        }

    }

    protected final void deflate() throws IOException {
        int len = this.def.deflate(this.buf, 0, this.buf.length);
        if (len > 0) {
            this.writeOut(this.buf, 0, len);
        }

    }

    protected void writeLocalFileHeader(ZipEntry ze) throws IOException {
        this.offsets.put(ze, new ZipLong(this.written));
        this.writeOut(LFH_SIG_bytes);
        this.written += 4L;
        if (ze.getMethod() == 8 && this.raf == null) {
            this.writeOut(Z20_bytes);
            if (this.isLefRequired()) {
                this.writeOut(Z2056_bytes);
            } else {
                this.writeOut(Z8_bytes);
            }
        } else {
            this.writeOut(Z10_bytes);
            if (this.isLefRequired()) {
                this.writeOut(Z2048_bytes);
            } else {
                this.writeOut(ZERO);
            }
        }

        this.written += 4L;
        this.writeOut(this.shortBytes(ze.getMethod()));
        this.written += 2L;
        this.writeOut(toDosTime(new Date(ze.getTime())).getBytes());
        this.written += 4L;
        this.localDataStart = this.written;
        if (ze.getMethod() != 8 && this.raf == null) {
            this.writeOut(this.longBytes(ze.getCrc()));
            this.writeOut(this.longBytes(ze.getSize()));
            this.writeOut(this.longBytes(ze.getSize()));
        } else {
            this.writeOut(LZERO);
            this.writeOut(LZERO);
            this.writeOut(LZERO);
        }

        this.written += 12L;
        byte[] name = this.getBytes(ze.getName());
        this.writeOut(this.shortBytes(name.length));
        this.written += 2L;
        byte[] extra = ze.getLocalFileDataExtra();
        this.writeOut(this.shortBytes(extra.length));
        this.written += 2L;
        this.writeOut(name);
        this.written += (long)name.length;
        this.writeOut(extra);
        this.written += (long)extra.length;
        this.dataStart = this.written;
    }

    protected void writeDataDescriptor(ZipEntry ze) throws IOException {
        if (ze.getMethod() == 8 && this.raf == null) {
            this.writeOut(DD_SIG_bytes);
            this.writeOut(this.longBytes(this.entry.getCrc()));
            this.writeOut(this.longBytes(this.entry.getCompressedSize()));
            this.writeOut(this.longBytes(this.entry.getSize()));
            this.written += 16L;
        }
    }

    protected void writeCentralFileHeader(ZipEntry ze) throws IOException {
        this.writeOut(CFH_SIG_bytes);
        this.written += 4L;
        this.writeOut(this.shortBytes(ze.getPlatform() << 8 | 20));
        this.written += 2L;
        if (ze.getMethod() == 8 && this.raf == null) {
            this.writeOut(Z20_bytes);
            if (this.isLefRequired()) {
                this.writeOut(Z2056_bytes);
            } else {
                this.writeOut(Z8_bytes);
            }
        } else {
            this.writeOut(Z10_bytes);
            if (this.isLefRequired()) {
                this.writeOut(Z2048_bytes);
            } else {
                this.writeOut(ZERO);
            }
        }

        this.written += 4L;
        this.writeOut(this.shortBytes(ze.getMethod()));
        this.written += 2L;
        this.writeOut(toDosTime(new Date(ze.getTime())).getBytes());
        this.written += 4L;
        this.writeOut(this.longBytes(ze.getCrc()));
        this.writeOut(this.longBytes(ze.getCompressedSize()));
        this.writeOut(this.longBytes(ze.getSize()));
        this.written += 12L;
        byte[] name = this.getBytes(ze.getName());
        this.writeOut(this.shortBytes(name.length));
        this.written += 2L;
        byte[] extra = ze.getCentralDirectoryExtra();
        this.writeOut(this.shortBytes(extra.length));
        this.written += 2L;
        String comm = ze.getComment();
        if (comm == null) {
            comm = "";
        }

        byte[] comment = this.getBytes(comm);
        this.writeOut(this.shortBytes(comment.length));
        this.written += 2L;
        this.writeOut(ZERO);
        this.written += 2L;
        this.writeOut(this.shortBytes(ze.getInternalAttributes()));
        this.written += 2L;
        this.writeOut(this.longBytes(ze.getExternalAttributes()));
        this.written += 4L;
        this.writeOut(((ZipLong)this.offsets.get(ze)).getBytes());
        this.written += 4L;
        this.writeOut(name);
        this.written += (long)name.length;
        this.writeOut(extra);
        this.written += (long)extra.length;
        this.writeOut(comment);
        this.written += (long)comment.length;
    }

    protected void writeCentralDirectoryEnd() throws IOException {
        this.writeOut(EOCD_SIG_bytes);
        this.writeOut(ZERO);
        this.writeOut(ZERO);
        byte[] num = this.shortBytes(this.entries.size());
        this.writeOut(num);
        this.writeOut(num);
        this.writeOut(this.cdLength.getBytes());
        this.writeOut(this.cdOffset.getBytes());
        byte[] data = this.getBytes(this.comment);
        this.writeOut(this.shortBytes(data.length));
        this.writeOut(data);
    }

    protected static ZipLong toDosTime(Date time) {
        int year = time.getYear() + 1900;
        int month = time.getMonth() + 1;
        if (year < 1980) {
            return DOS_TIME_MIN;
        } else {
            long value = (long)(year - 1980 << 25 | month << 21 | time.getDate() << 16 | time.getHours() << 11 | time.getMinutes() << 5 | time.getSeconds() >> 1);
            byte[] result = new byte[]{(byte)((int)(value & 255L)), (byte)((int)((value & 65280L) >> 8)), (byte)((int)((value & 16711680L) >> 16)), (byte)((int)((value & 4278190080L) >> 24))};
            return new ZipLong(result);
        }
    }

    protected byte[] getBytes(String name) throws ZipException {
        if (this.encoding == null) {
            return name.getBytes();
        } else {
            try {
                return name.getBytes(this.encoding);
            } catch (UnsupportedEncodingException var3) {
                throw new ZipException(var3.getMessage());
            }
        }
    }

    protected final void writeOut(byte[] data) throws IOException {
        this.writeOut(data, 0, data.length);
    }

    protected final void writeOut(byte[] data, int offset, int length) throws IOException {
        if (this.raf != null) {
            this.raf.write(data, offset, length);
        } else {
            this.out.write(data, offset, length);
        }

    }

    final byte[] shortBytes(int value) {
        return ZipShort.bytes(value, this.shortBuffer);
    }

    final byte[] longBytes(long value) {
        return ZipLong.bytes(value, this.longBuffer);
    }

    private boolean isLefRequired() {
        return this.encoding == null && "UTF-8".equals(Charset.defaultCharset().name()) || this.encoding != null && "UTF-8".equals(this.encoding);
    }

    static {
        LFH_SIG_bytes = LFH_SIG.getBytes();
        DD_SIG_bytes = DD_SIG.getBytes();
        CFH_SIG_bytes = CFH_SIG.getBytes();
        EOCD_SIG_bytes = EOCD_SIG.getBytes();
        DOS_TIME_MIN = new ZipLong(8448L);
    }
}
