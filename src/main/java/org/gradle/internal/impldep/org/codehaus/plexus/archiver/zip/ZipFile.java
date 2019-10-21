//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip;

import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiveFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class ZipFile implements ArchiveFile
{
    private final Hashtable<ZipEntry, Long> entries;
    private final Hashtable<String, ZipEntry> nameMap;
    private Hashtable<ZipEntry, Long> dataOffsets;
    private String encoding;
    private final RandomAccessFile archive;
    private static final int CFH_LEN = 42;
    private static final int MIN_EOCD_SIZE = 22;
    private static final int CFD_LOCATOR_OFFSET = 16;
    private static final long LFH_OFFSET_FOR_FILENAME_LENGTH = 26L;
    static final ThreadLocal threadCalander = new ThreadLocal() {
        protected Object initialValue() {
            return Calendar.getInstance();
        }
    };

    public ZipFile(File f) throws IOException {
        this((File)f, (String)null);
    }

    public ZipFile(String name) throws IOException {
        this((File)(new File(name)), (String)null);
    }

    public ZipFile(String name, String encoding) throws IOException {
        this(new File(name), encoding);
    }

    public ZipFile(File f, String encoding) throws IOException {
        this.entries = new Hashtable();
        this.nameMap = new Hashtable();
        this.dataOffsets = new Hashtable();
        this.encoding = null;
        this.encoding = encoding;
        this.archive = new RandomAccessFile(f, "r");
        this.populateFromCentralDirectory();
        this.resolveLocalFileHeaderData();
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void close() throws IOException {
        this.archive.close();
    }

    public Enumeration<ZipEntry> getEntries() {
        return this.entries.keys();
    }

    public ZipEntry getEntry(String name) {
        return (ZipEntry)this.nameMap.get(name);
    }

    public InputStream getInputStream(Entry entry) throws IOException {
        return this.getInputStream((ZipEntry)entry);
    }

    public InputStream getInputStream(ZipEntry ze) throws IOException {
        Long start = (Long)this.dataOffsets.get(ze);
        if (start == null) {
            return null;
        } else {
            ZipFile.BoundedInputStream bis = new ZipFile.BoundedInputStream(start, ze.getCompressedSize());
            switch(ze.getMethod()) {
                case 0:
                    return bis;
                case 8:
                    bis.addDummy();
                    return new InflaterInputStream(bis, new Inflater(true)) {
                        public void close() throws IOException {
                            super.close();
                            this.inf.end();
                        }
                    };
                default:
                    throw new ZipException("Found unsupported compression method " + ze.getMethod());
            }
        }
    }

    private void populateFromCentralDirectory() throws IOException {
        this.positionAtCentralDirectory();
        byte[] cfh = new byte[42];
        byte[] signatureBytes = new byte[4];
        this.archive.readFully(signatureBytes);

        for(ZipLong sig = new ZipLong(signatureBytes); sig.equals(ZipOutputStream.CFH_SIG); sig = new ZipLong(signatureBytes)) {
            this.archive.readFully(cfh);
            int off0 = 0;
            ZipEntry ze = new ZipEntry();
            ze.setPlatform(ZipShort.convert(cfh, off0) >> 8 & 15);
            int off = off0 + 2;
            off += 4;
            ze.setMethod(ZipShort.convert(cfh, off));
            off += 2;
            ze.setTime(fromDosTime(ZipLong.convert(cfh, off)).getTime());
            off += 4;
            ze.setCrc(ZipLong.convert(cfh, off));
            off += 4;
            ze.setCompressedSize(ZipLong.convert(cfh, off));
            off += 4;
            ze.setSize(ZipLong.convert(cfh, off));
            off += 4;
            int fileNameLen = ZipShort.convert(cfh, off);
            off += 2;
            int extraLen = ZipShort.convert(cfh, off);
            off += 2;
            int commentLen = ZipShort.convert(cfh, off);
            off += 2;
            off += 2;
            ze.setInternalAttributes(ZipShort.convert(cfh, off));
            off += 2;
            ze.setExternalAttributes(ZipLong.convert(cfh, off));
            off += 4;
            this.entries.put(ze, new Long(ZipLong.convert(cfh, off)));
            byte[] fileName = new byte[fileNameLen];
            this.archive.readFully(fileName);
            ze.setName(this.getString(fileName));
            this.nameMap.put(ze.getName(), ze);
            this.archive.skipBytes(extraLen);
            byte[] comment = new byte[commentLen];
            this.archive.readFully(comment);
            ze.setComment(this.getString(comment));
            this.archive.readFully(signatureBytes);
        }

    }

    private void positionAtCentralDirectory() throws IOException {
        long off = this.archive.length() - 22L;
        this.archive.seek(off);
        byte[] sig = ZipOutputStream.EOCD_SIG.getBytes();
        int curr = this.archive.read();

        boolean found;
        for(found = false; curr != -1; curr = this.archive.read()) {
            if (curr == sig[0]) {
                curr = this.archive.read();
                if (curr == sig[1]) {
                    curr = this.archive.read();
                    if (curr == sig[2]) {
                        curr = this.archive.read();
                        if (curr == sig[3]) {
                            found = true;
                            break;
                        }
                    }
                }
            }

            this.archive.seek(--off);
        }

        if (!found) {
            throw new ZipException("archive is not a ZIP archive");
        } else {
            this.archive.seek(off + 16L);
            byte[] cfdOffset = new byte[4];
            this.archive.readFully(cfdOffset);
            this.archive.seek(ZipLong.convert(cfdOffset));
        }
    }

    private void resolveLocalFileHeaderData() throws IOException {
        this.dataOffsets = new Hashtable(this.entries.size());
        byte[] b = new byte[2];
        Iterator i$ = this.entries.entrySet().iterator();

        while(i$.hasNext()) {
            java.util.Map.Entry<ZipEntry, Long> e = (java.util.Map.Entry)i$.next();
            ZipEntry ze = (ZipEntry)e.getKey();
            long offset = (Long)e.getValue();
            this.archive.seek(offset + 26L);
            this.archive.readFully(b);
            int fileNameLen = ZipShort.convert(b);
            this.archive.readFully(b);
            int extraFieldLen = ZipShort.convert(b);
            this.archive.skipBytes(fileNameLen);
            byte[] localExtraData = new byte[extraFieldLen];
            this.archive.readFully(localExtraData);
            ze.setExtra(localExtraData);
            this.dataOffsets.put(ze, offset + 26L + 2L + 2L + (long)fileNameLen + (long)extraFieldLen);
        }

    }

    protected static Date fromDosTime(ZipLong l) {
        long dosTime = l.getValue();
        return fromDosTime(dosTime);
    }

    protected static Date fromDosTime(long dosTime) {
        Calendar cal = (Calendar)threadCalander.get();
        cal.set(1, (int)(dosTime >> 25 & 127L) + 1980);
        cal.set(2, (int)(dosTime >> 21 & 15L) - 1);
        cal.set(5, (int)(dosTime >> 16) & 31);
        cal.set(11, (int)(dosTime >> 11) & 31);
        cal.set(12, (int)(dosTime >> 5) & 63);
        cal.set(13, (int)(dosTime << 1) & 62);
        return cal.getTime();
    }

    protected String getString(byte[] bytes) throws ZipException {
        if (this.encoding == null) {
            return new String(bytes);
        } else {
            try {
                return new String(bytes, this.encoding);
            } catch (UnsupportedEncodingException var3) {
                throw new ZipException(var3.getMessage());
            }
        }
    }

    private class BoundedInputStream extends InputStream {
        private long remaining;
        private long loc;
        private boolean addDummyByte = false;

        BoundedInputStream(long start, long remaining) {
            this.remaining = remaining;
            this.loc = start;
        }

        public int read() throws IOException {
            if (this.remaining-- <= 0L) {
                if (this.addDummyByte) {
                    this.addDummyByte = false;
                    return 0;
                } else {
                    return -1;
                }
            } else {
                synchronized(ZipFile.this.archive) {
                    ZipFile.this.archive.seek((long)(this.loc++));
                    return ZipFile.this.archive.read();
                }
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (this.remaining <= 0L) {
                if (this.addDummyByte) {
                    this.addDummyByte = false;
                    b[off] = 0;
                    return 1;
                } else {
                    return -1;
                }
            } else if (len <= 0) {
                return 0;
            } else {
                if ((long)len > this.remaining) {
                    len = (int)this.remaining;
                }

                int ret;
                synchronized(ZipFile.this.archive) {
                    ZipFile.this.archive.seek(this.loc);
                    ret = ZipFile.this.archive.read(b, off, len);
                }

                if (ret > 0) {
                    this.loc += (long)ret;
                    this.remaining -= (long)ret;
                }

                return ret;
            }
        }

        void addDummy() {
            this.addDummyByte = true;
        }
    }
}
