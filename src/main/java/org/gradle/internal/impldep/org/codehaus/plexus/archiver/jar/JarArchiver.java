//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.tools.ant.types.EnumeratedAttribute;
import org.gradle.internal.impldep.org.apache.tools.zip.ZipEntry;
import org.gradle.internal.impldep.org.apache.tools.zip.ZipFile;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiverException;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.gradle.internal.impldep.org.apache.tools.zip.ZipOutputStream;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.console.ConsoleLogger;
import org.gradle.internal.impldep.org.codehaus.plexus.util.IOUtil;

public class JarArchiver extends ZipArchiver
{
    private static final String META_INF_NAME = "META-INF";
    private static final String INDEX_NAME = "META-INF/INDEX.LIST";
    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private Manifest configuredManifest;
    private Manifest savedConfiguredManifest;
    private Manifest filesetManifest;
    private Manifest originalManifest;
    private JarArchiver.FilesetManifestConfig filesetManifestConfig;
    private boolean mergeManifestsMain = true;
    private Manifest manifest;
    private File manifestFile;
    private boolean index = false;
    private boolean createEmpty = false;
    private Vector<String> rootEntries;
    private ArrayList<String> indexJars;

    public JarArchiver() {
        this.archiveType = "jar";
        this.setEncoding("UTF8");
        this.rootEntries = new Vector();
    }

    public void setIndex(boolean flag) {
        this.index = flag;
    }

    /** @deprecated */
    @Deprecated
    public void setManifestEncoding(String manifestEncoding) {
    }

    public void addConfiguredManifest(Manifest newManifest) throws ManifestException {
        if (this.configuredManifest == null) {
            this.configuredManifest = newManifest;
        } else {
            JdkManifestFactory.merge(this.configuredManifest, newManifest, false);
        }

        this.savedConfiguredManifest = this.configuredManifest;
    }

    public void setManifest(File manifestFile) throws ArchiverException {
        if (!manifestFile.exists()) {
            throw new ArchiverException("Manifest file: " + manifestFile + " does not exist.");
        } else {
            this.manifestFile = manifestFile;
        }
    }

    private Manifest getManifest(File manifestFile) throws ArchiverException {
        FileInputStream in = null;

        Manifest var3;
        try {
            in = new FileInputStream(manifestFile);
            var3 = this.getManifest((InputStream)in);
        } catch (IOException var7) {
            throw new ArchiverException("Unable to read manifest file: " + manifestFile + " (" + var7.getMessage() + ")", var7);
        } finally {
            IOUtil.close(in);
        }

        return var3;
    }

    private Manifest getManifest(InputStream is) throws ArchiverException {
        try {
            return new Manifest(is);
        } catch (IOException var3) {
            throw new ArchiverException("Unable to read manifest file (" + var3.getMessage() + ")", var3);
        }
    }

    public void setFilesetmanifest(JarArchiver.FilesetManifestConfig config) {
        this.filesetManifestConfig = config;
        // FIXME: this.mergeManifestsMain = "merge".equals(config.getValue());
        // if (this.filesetManifestConfig != null && !this.filesetManifestConfig.getValue().equals("skip")) {
            this.doubleFilePass = true;
        // }

    }

    public void addConfiguredIndexJars(File indexJar) {
        if (this.indexJars == null) {
            this.indexJars = new ArrayList();
        }

        this.indexJars.add(indexJar.getAbsolutePath());
    }

    protected void initZipOutputStream(ZipOutputStream zOut) throws IOException, ArchiverException {
        if (!this.skipWriting) {
            Manifest jarManifest = this.createManifest();
            this.writeManifest(zOut, jarManifest);
        }

    }

    protected boolean hasVirtualFiles() {
        this.getLogger().debug("\n\n\nChecking for jar manifest virtual files...\n\n\n");
        System.out.flush();
        return this.configuredManifest != null || this.manifest != null || this.manifestFile != null || super.hasVirtualFiles();
    }

    private Manifest createManifest() throws ArchiverException {
        Manifest finalManifest = Manifest.getDefaultManifest();
        if (this.manifest == null && this.manifestFile != null) {
            this.manifest = this.getManifest(this.manifestFile);
        }

        if (this.isInUpdateMode()) {
            JdkManifestFactory.merge(finalManifest, this.originalManifest, false);
        }

        JdkManifestFactory.merge(finalManifest, this.filesetManifest, false);
        JdkManifestFactory.merge(finalManifest, this.configuredManifest, false);
        JdkManifestFactory.merge(finalManifest, this.manifest, !this.mergeManifestsMain);
        return finalManifest;
    }

    private void writeManifest(ZipOutputStream zOut, Manifest manifest) throws IOException, ArchiverException {
        Enumeration e = manifest.getWarnings();

        while(e.hasMoreElements()) {
            this.getLogger().warn("Manifest warning: " + e.nextElement());
        }

        this.zipDir((PlexusIoResource)null, zOut, "META-INF/", 16877);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        manifest.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        super.zipFile(bais, zOut, "META-INF/MANIFEST.MF", System.currentTimeMillis(), (File)null, 33188);
        super.initZipOutputStream(zOut);
    }

    protected void finalizeZipOutputStream(ZipOutputStream zOut) throws IOException, ArchiverException {
        if (this.index) {
            this.createIndexList(zOut);
        }

    }

    private void createIndexList(ZipOutputStream zOut) throws IOException, ArchiverException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, "UTF8"));
        writer.println("JarIndex-Version: 1.0");
        writer.println();
        writer.println(this.getDestFile().getName());
        Set<String> filteredDirs = new HashSet(this.addedDirs.keySet());
        if (filteredDirs.contains("META-INF/")) {
            boolean add = false;
            Iterator i$ = this.entries.keySet().iterator();

            while(i$.hasNext()) {
                String entry = (String)i$.next();
                if (entry.startsWith("META-INF/") && !entry.equals("META-INF/INDEX.LIST") && !entry.equals("META-INF/MANIFEST.MF")) {
                    add = true;
                    break;
                }
            }

            if (!add) {
                filteredDirs.remove("META-INF/");
            }
        }

        this.writeIndexLikeList(new ArrayList(filteredDirs), this.rootEntries, writer);
        writer.println();
        if (this.indexJars != null) {
            java.util.jar.Manifest mf = this.createManifest();
            String classpath = mf.getMainAttributes().getValue("Class-Path");
            String[] cpEntries = null;
            if (classpath != null) {
                StringTokenizer tok = new StringTokenizer(classpath, " ");
                cpEntries = new String[tok.countTokens()];

                for(int var9 = 0; tok.hasMoreTokens(); cpEntries[var9++] = tok.nextToken()) {
                }
            }

            Iterator i$ = this.indexJars.iterator();

            while(i$.hasNext()) {
                String indexJar = (String)i$.next();
                String name = findJarName(indexJar, cpEntries);
                if (name != null) {
                    ArrayList<String> dirs = new ArrayList();
                    ArrayList<String> files = new ArrayList();
                    grabFilesAndDirs(indexJar, dirs, files);
                    if (dirs.size() + files.size() > 0) {
                        writer.println(name);
                        this.writeIndexLikeList(dirs, files, writer);
                        writer.println();
                    }
                }
            }
        }

        writer.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        super.zipFile(bais, zOut, "META-INF/INDEX.LIST", System.currentTimeMillis(), (File)null, 33188);
    }

    protected void zipFile(InputStream is, ZipOutputStream zOut, String vPath, long lastModified, File fromArchive, int mode) throws IOException, ArchiverException {
        if ("META-INF/MANIFEST.MF".equalsIgnoreCase(vPath)) {
            if (!this.doubleFilePass || this.skipWriting) {
                this.filesetManifest(fromArchive, is);
            }
        } else if ("META-INF/INDEX.LIST".equalsIgnoreCase(vPath) && this.index) {
            this.getLogger().warn("Warning: selected " + this.archiveType + " files include a META-INF/INDEX.LIST which will" + " be replaced by a newly generated one.");
        } else {
            if (this.index && !vPath.contains("/")) {
                this.rootEntries.addElement(vPath);
            }

            super.zipFile(is, zOut, vPath, lastModified, fromArchive, mode);
        }

    }

    private void filesetManifest(File file, InputStream is) throws ArchiverException {
        if (this.manifestFile != null && this.manifestFile.equals(file)) {
            this.getLogger().debug("Found manifest " + file);
            if (is != null) {
                this.manifest = this.getManifest(is);
            } else {
                this.manifest = this.getManifest(file);
            }
        // FIXME } else if (this.filesetManifestConfig != null && !this.filesetManifestConfig.getValue().equals("skip")) {
        } else if (this.filesetManifestConfig != null ) {
            this.getLogger().debug("Found manifest to merge in file " + file);
            Manifest newManifest;
            if (is != null) {
                newManifest = this.getManifest(is);
            } else {
                newManifest = this.getManifest(file);
            }

            if (this.filesetManifest == null) {
                this.filesetManifest = newManifest;
            } else {
                JdkManifestFactory.merge(this.filesetManifest, newManifest, false);
            }
        }

    }

    protected boolean createEmptyZip(File zipFile) throws ArchiverException {
        if (!this.createEmpty) {
            return true;
        } else {
            ZipOutputStream zOut = null;

            try {
                this.getLogger().debug("Building MANIFEST-only jar: " + this.getDestFile().getAbsolutePath());
                FileOutputStream out = new FileOutputStream(this.getDestFile());
                zOut = new ZipOutputStream(new BufferedOutputStream(out, 65536));
                zOut.setEncoding(this.getEncoding());
                if (this.isCompress()) {
                    zOut.setMethod(8);
                } else {
                    zOut.setMethod(0);
                }

                this.initZipOutputStream(zOut);
                this.finalizeZipOutputStream(zOut);
            } catch (IOException var7) {
                throw new ArchiverException("Could not create almost empty JAR archive (" + var7.getMessage() + ")", var7);
            } finally {
                IOUtil.close(zOut);
                this.createEmpty = false;
            }

            return true;
        }
    }

    protected void cleanUp() {
        super.cleanUp();
        if (!this.doubleFilePass || !this.skipWriting) {
            this.manifest = null;
            this.configuredManifest = this.savedConfiguredManifest;
            this.filesetManifest = null;
            this.originalManifest = null;
        }

        this.rootEntries.removeAllElements();
    }

    public void reset() {
        super.reset();
        this.configuredManifest = null;
        this.filesetManifestConfig = null;
        this.mergeManifestsMain = false;
        this.manifestFile = null;
        this.index = false;
    }

    protected final void writeIndexLikeList(List<String> dirs, List<String> files, PrintWriter writer) {
        Collections.sort(dirs);
        Collections.sort(files);

        Iterator iter;
        String dir;
        for(iter = dirs.iterator(); iter.hasNext(); writer.println(dir)) {
            dir = (String)iter.next();
            dir = dir.replace('\\', '/');
            if (dir.startsWith("./")) {
                dir = dir.substring(2);
            }

            while(dir.startsWith("/")) {
                dir = dir.substring(1);
            }

            int pos = dir.lastIndexOf(47);
            if (pos != -1) {
                dir = dir.substring(0, pos);
            }
        }

        iter = files.iterator();

        while(iter.hasNext()) {
            writer.println(iter.next());
        }

    }

    protected static String findJarName(String fileName, String[] classpath) {
        if (classpath == null) {
            return (new File(fileName)).getName();
        } else {
            fileName = fileName.replace(File.separatorChar, '/');
            SortedMap<String, String> matches = new TreeMap(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1 != null && o2 != null ? o2.length() - o1.length() : 0;
                }
            });
            String[] arr$ = classpath;
            int len$ = classpath.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                String aClasspath = arr$[i$];
                if (fileName.endsWith(aClasspath)) {
                    matches.put(aClasspath, aClasspath);
                } else {
                    int slash = aClasspath.indexOf("/");

                    for(String candidate = aClasspath; slash > -1; slash = candidate.indexOf("/")) {
                        candidate = candidate.substring(slash + 1);
                        if (fileName.endsWith(candidate)) {
                            matches.put(candidate, aClasspath);
                            break;
                        }
                    }
                }
            }

            return matches.size() == 0 ? null : (String)matches.get(matches.firstKey());
        }
    }

    protected static void grabFilesAndDirs(String file, List<String> dirs, List<String> files) throws IOException {
        File zipFile = new File(file);
        ConsoleLogger logger;
        if (!zipFile.exists()) {
            logger = new ConsoleLogger(1, "console");
            logger.error("JarArchive skipping non-existing file: " + zipFile.getAbsolutePath());
        } else if (zipFile.isDirectory()) {
            logger = new ConsoleLogger(1, "console");
            logger.info("JarArchiver skipping indexJar " + zipFile + " because it is not a jar");
        } else {
            ZipFile zf = null;

            try {
                zf = new ZipFile(file, "utf-8");
                Enumeration<ZipEntry> entries = zf.getEntries();
                HashSet dirSet = new HashSet();

                while(entries.hasMoreElements()) {
                    ZipEntry ze = (ZipEntry)entries.nextElement();
                    String name = ze.getName();
                    if (!name.equals("META-INF") && !name.equals("META-INF/") && !name.equals("META-INF/INDEX.LIST") && !name.equals("META-INF/MANIFEST.MF")) {
                        if (ze.isDirectory()) {
                            dirSet.add(name);
                        } else if (!name.contains("/")) {
                            files.add(name);
                        } else {
                            dirSet.add(name.substring(0, name.lastIndexOf("/") + 1));
                        }
                    }
                }

                dirs.addAll(dirSet);
            } finally {
                if (zf != null) {
                    zf.close();
                }

            }
        }

    }

    public static class FilesetManifestConfig extends EnumeratedAttribute
    {
        public FilesetManifestConfig() {
        }

        public String[] getValues() {
            return new String[]{"skip", "merge", "mergewithoutmain"};
        }
    }
}
