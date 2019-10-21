//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip;

import org.gradle.internal.impldep.org.apache.tools.zip.ZipOutputStream;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.AbstractArchiver;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiveEntry;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiverException;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ResourceIterator;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.util.ResourceUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoFileResource;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.gradle.internal.impldep.org.codehaus.plexus.util.FileUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.util.IOUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.zip.CRC32;

public abstract class AbstractZipArchiver extends AbstractArchiver
{
    private String comment;
    private String encoding;
    private boolean doCompress = true;
    private boolean recompressAddedZips = true;
    private boolean doUpdate = false;
    private boolean savedDoUpdate = false;
    protected String archiveType = "zip";
    private boolean doFilesonly = false;
    protected Hashtable<String, String> entries = new Hashtable();
    protected Hashtable<String, String> addedDirs = new Hashtable();
    private static final long EMPTY_CRC = (new CRC32()).getValue();
    protected boolean doubleFilePass = false;
    protected boolean skipWriting = false;
    /** @deprecated */
    protected String duplicate = "skip";
    protected boolean addingNewFiles = false;
    private boolean roundUp = true;
    private File renamedFile = null;
    private File zipFile;
    private boolean success;
    private ZipOutputStream zOut;

    public AbstractZipArchiver() {
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setCompress(boolean compress) {
        this.doCompress = compress;
    }

    public boolean isCompress() {
        return this.doCompress;
    }

    public boolean isRecompressAddedZips() {
        return this.recompressAddedZips;
    }

    public void setRecompressAddedZips(boolean recompressAddedZips) {
        this.recompressAddedZips = recompressAddedZips;
    }

    public void setUpdateMode(boolean update) {
        this.doUpdate = update;
        this.savedDoUpdate = this.doUpdate;
    }

    public boolean isInUpdateMode() {
        return this.doUpdate;
    }

    public void setFilesonly(boolean f) {
        this.doFilesonly = f;
    }

    public boolean isFilesonly() {
        return this.doFilesonly;
    }

    public void setRoundUp(boolean r) {
        this.roundUp = r;
    }

    public boolean isRoundUp() {
        return this.roundUp;
    }

    protected void execute() throws ArchiverException, IOException {
        if (this.checkForced()) {
            if (this.doubleFilePass) {
                this.skipWriting = true;
                this.createArchiveMain();
                this.skipWriting = false;
                this.createArchiveMain();
            } else {
                this.createArchiveMain();
            }

            this.finalizeZipOutputStream(this.zOut);
        }
    }

    protected void finalizeZipOutputStream(ZipOutputStream zOut) throws IOException, ArchiverException {
    }

    private void createArchiveMain() throws ArchiverException, IOException {
        if (!"skip".equals(this.duplicate)) {
            this.setDuplicateBehavior(this.duplicate);
        }

        ResourceIterator iter = this.getResources();
        if (!iter.hasNext() && !this.hasVirtualFiles()) {
            throw new ArchiverException("You must set at least one file.");
        } else {
            this.zipFile = this.getDestFile();
            if (this.zipFile == null) {
                throw new ArchiverException("You must set the destination " + this.archiveType + "file.");
            } else if (this.zipFile.exists() && !this.zipFile.isFile()) {
                throw new ArchiverException(this.zipFile + " isn't a file.");
            } else if (this.zipFile.exists() && !this.zipFile.canWrite()) {
                throw new ArchiverException(this.zipFile + " is read-only.");
            } else {
                this.addingNewFiles = true;
                if (this.doUpdate && !this.zipFile.exists()) {
                    this.doUpdate = false;
                    this.getLogger().debug("ignoring update attribute as " + this.archiveType + " doesn't exist.");
                }

                this.success = false;
                if (this.doUpdate) {
                    this.renamedFile = FileUtils.createTempFile("zip", ".tmp", this.zipFile.getParentFile());
                    this.renamedFile.deleteOnExit();

                    try {
                        FileUtils.rename(this.zipFile, this.renamedFile);
                    } catch (SecurityException var5) {
                        this.getLogger().debug(var5.toString());
                        throw new ArchiverException("Not allowed to rename old file (" + this.zipFile.getAbsolutePath() + ") to temporary file", var5);
                    } catch (IOException var6) {
                        this.getLogger().debug(var6.toString());
                        throw new ArchiverException("Unable to rename old file (" + this.zipFile.getAbsolutePath() + ") to temporary file", var6);
                    }
                }

                String action = this.doUpdate ? "Updating " : "Building ";
                this.getLogger().info(action + this.archiveType + ": " + this.zipFile.getAbsolutePath());
                if (!this.skipWriting) {
                    FileOutputStream out = new FileOutputStream(this.zipFile);
                    BufferedOutputStream buffered = new BufferedOutputStream(out, 65536);
                    this.zOut = new ZipOutputStream(buffered);
                    this.zOut.setEncoding(this.encoding);
                    if (this.doCompress) {
                        this.zOut.setMethod(8);
                    } else {
                        this.zOut.setMethod(0);
                    }
                }

                this.initZipOutputStream(this.zOut);
                this.addResources(iter, this.zOut);
                if (this.doUpdate && !this.renamedFile.delete()) {
                    this.getLogger().warn("Warning: unable to delete temporary file " + this.renamedFile.getName());
                }

                this.success = true;
            }
        }
    }

    protected Map<String, Long> getZipEntryNames(File file) throws IOException {
        if (file.exists() && this.doUpdate) {
            Map<String, Long> entries = new HashMap();
            ZipFile zipFile = new ZipFile(file);
            Enumeration en = zipFile.getEntries();

            while(en.hasMoreElements()) {
                ZipEntry ze = (ZipEntry)en.nextElement();
                entries.put(ze.getName(), ze.getLastModificationTime());
            }

            return entries;
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    protected static boolean isFileAdded(ArchiveEntry entry, Map entries) {
        return !entries.containsKey(entry.getName());
    }

    protected static boolean isFileUpdated(ArchiveEntry entry, Map entries) {
        Long l = (Long)entries.get(entry.getName());
        return l != null && (l == -1L || !ResourceUtils.isUptodate(entry.getResource(), l));
    }

    protected final void addResources(ResourceIterator resources, ZipOutputStream zOut) throws IOException, ArchiverException {
        Object base = null;

        while(resources.hasNext()) {
            ArchiveEntry entry = resources.next();
            String name = entry.getName();
            name = name.replace(File.separatorChar, '/');
            if (!"".equals(name)) {
                if (entry.getResource().isDirectory() && !name.endsWith("/")) {
                    name = name + "/";
                }

                this.addParentDirs((File)base, name, zOut, "");
                if (entry.getResource().isFile()) {
                    this.zipFile(entry, zOut, name);
                } else {
                    this.zipDir(entry.getResource(), zOut, name, entry.getMode());
                }
            }
        }

    }

    protected final void addParentDirs(File baseDir, String entry, ZipOutputStream zOut, String prefix) throws IOException {
        if (!this.doFilesonly && this.getIncludeEmptyDirs()) {
            Stack<String> directories = new Stack();
            int slashPos = entry.length() - (entry.endsWith("/") ? 1 : 0);

            String dir;
            while((slashPos = entry.lastIndexOf(47, slashPos - 1)) != -1) {
                dir = entry.substring(0, slashPos + 1);
                if (this.addedDirs.contains(prefix + dir)) {
                    break;
                }

                directories.push(dir);
            }

            while(!directories.isEmpty()) {
                dir = (String)directories.pop();
                File f;
                if (baseDir != null) {
                    f = new File(baseDir, dir);
                } else {
                    f = new File(dir);
                }

                PlexusIoFileResource res = new PlexusIoFileResource(f);
                this.zipDir(res, zOut, prefix + dir, this.getRawDefaultDirectoryMode());
            }
        }

    }

    private void readWithZipStats(InputStream in, byte[] header, ZipEntry ze, ByteArrayOutputStream bos) throws IOException {
        byte[] buffer = new byte[8192];
        CRC32 cal2 = new CRC32();
        long size = 0L;
        byte[] arr$ = header;
        int len$ = header.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            byte aHeader = arr$[i$];
            cal2.update(aHeader);
            ++size;
        }

        int count = 0;

        do {
            size += (long)count;
            cal2.update(buffer, 0, count);
            if (bos != null) {
                bos.write(buffer, 0, count);
            }

            count = in.read(buffer, 0, buffer.length);
        } while(count != -1);

        ze.setSize(size);
        ze.setCrc(cal2.getValue());
    }

    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long size = 0L;

        int n;
        while(-1 != (n = input.read(buffer))) {
            size += (long)n;
            output.write(buffer, 0, n);
        }

        return size;
    }

    protected void zipFile(InputStream in, ZipOutputStream zOut, String vPath, long lastModified, File fromArchive, int mode) throws IOException, ArchiverException {
        this.getLogger().debug("adding entry " + vPath);
        this.entries.put(vPath, vPath);
        if (!this.skipWriting) {
            ZipEntry ze = new ZipEntry(vPath);
            ze.setTime(lastModified);
            byte[] header = new byte[4];
            int read = in.read(header);
            boolean compressThis = this.doCompress;
            if (!this.recompressAddedZips && this.isZipHeader(header)) {
                compressThis = false;
            }

            ze.setMethod(compressThis ? 8 : 0);
            ze.setUnixMode('耀' | mode);

            if (!zOut.isSeekable() && !compressThis) {
                if (in.markSupported()) {
                    in.mark(2147483647);
                    this.readWithZipStats(in, header, ze, (ByteArrayOutputStream)null);
                    in.reset();
                    // FIXME: zOut.putNextEntry(ze);
                    if (read > 0) {
                        zOut.write(header, 0, read);
                    }

                    IOUtil.copy(in, zOut, 8192);
                } else {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(131072);
                    this.readWithZipStats(in, header, ze, bos);
                    // FIXME: zOut.putNextEntry(ze);
                    if (read > 0) {
                        zOut.write(header, 0, read);
                    }

                    bos.writeTo(zOut);
                }
            } else {
                // FIXME: zOut.putNextEntry(ze);
                if (read > 0) {
                    zOut.write(header, 0, read);
                }

                IOUtil.copy(in, zOut, 8192);
            }
        }

    }

    private boolean isZipHeader(byte[] header) {
        return header[0] == 80 && header[1] == 75 && header[2] == 3 && header[3] == 4;
    }

    protected void zipFile(ArchiveEntry entry, ZipOutputStream zOut, String vPath) throws IOException, ArchiverException {
        if (ResourceUtils.isSame(entry.getResource(), this.getDestFile())) {
            throw new ArchiverException("A zip file cannot include itself");
        } else {
            InputStream in = entry.getInputStream();

            try {
                long lastModified = entry.getResource().getLastModified() + (long)(this.roundUp ? 1999 : 0);
                this.zipFile(in, zOut, vPath, lastModified, (File)null, entry.getMode());
            } catch (IOException var10) {
                throw new ArchiverException("IOException when zipping " + entry.getName() + ": " + var10.getMessage(), var10);
            } finally {
                IOUtil.close(in);
            }

        }
    }

    protected void zipDir(PlexusIoResource dir, ZipOutputStream zOut, String vPath, int mode) throws IOException {
        if (this.addedDirs.get(vPath) == null) {
            this.getLogger().debug("adding directory " + vPath);
            this.addedDirs.put(vPath, vPath);
            if (!this.skipWriting) {
                ZipEntry ze = new ZipEntry(vPath);
                if (dir != null && dir.isExisting()) {
                    long lastModified = dir.getLastModified() + (long)(this.roundUp ? 1999 : 0);
                    ze.setTime(lastModified);
                } else {
                    ze.setTime(System.currentTimeMillis() + (long)(this.roundUp ? 1999 : 0));
                }

                ze.setSize(0L);
                ze.setMethod(0);
                ze.setCrc(EMPTY_CRC);
                ze.setUnixMode(mode);
                // FIXME zOut.putNextEntry((org.gradle.internal.impldep.org.apache.tools.zip.ZipEntry) ze);
            }

        }
    }

    protected boolean createEmptyZip(File zipFile) throws ArchiverException {
        this.getLogger().info("Note: creating empty " + this.archiveType + " archive " + zipFile);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(zipFile);
            byte[] empty = new byte[22];
            empty[0] = 80;
            empty[1] = 75;
            empty[2] = 5;
            empty[3] = 6;
            os.write(empty);
        } catch (IOException var7) {
            throw new ArchiverException("Could not create empty ZIP archive (" + var7.getMessage() + ")", var7);
        } finally {
            IOUtil.close(os);
        }

        return true;
    }

    protected void cleanUp() {
        super.cleanUp();
        this.addedDirs.clear();
        this.entries.clear();
        this.addingNewFiles = false;
        this.doUpdate = this.savedDoUpdate;
        this.success = false;
        this.zOut = null;
        this.renamedFile = null;
        this.zipFile = null;
    }

    public void reset() {
        this.setDestFile((File)null);
        this.archiveType = "zip";
        this.doCompress = true;
        this.doUpdate = false;
        this.doFilesonly = false;
        this.encoding = null;
    }

    protected void initZipOutputStream(ZipOutputStream zOut) throws IOException, ArchiverException {
    }

    public boolean isSupportingForced() {
        return true;
    }

    protected boolean revert(StringBuffer messageBuffer) {
        int initLength = messageBuffer.length();
        if ((!this.doUpdate || this.renamedFile != null) && !this.zipFile.delete()) {
            messageBuffer.append(" (and the archive is probably corrupt but I could not delete it)");
        }

        if (this.doUpdate && this.renamedFile != null) {
            try {
                FileUtils.rename(this.renamedFile, this.zipFile);
            } catch (IOException var4) {
                messageBuffer.append(" (and I couldn't rename the temporary file ");
                messageBuffer.append(this.renamedFile.getName());
                messageBuffer.append(" back)");
            }
        }

        return messageBuffer.length() == initLength;
    }

    protected void close() throws IOException {
        try {
            if (this.zOut != null) {
                this.zOut.close();
            }
        } catch (IOException var2) {
            if (this.success) {
                throw var2;
            }
        }

    }

    protected String getArchiveType() {
        return this.archiveType;
    }
}
