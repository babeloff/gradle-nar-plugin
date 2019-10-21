//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import org.gradle.internal.impldep.org.codehaus.plexus.PlexusContainer;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.util.DefaultArchivedFileSet;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.util.FilterSupport;
import org.gradle.internal.impldep.org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.*;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.proxy.PlexusIoProxyResourceCollection;
import org.gradle.internal.impldep.org.codehaus.plexus.context.Context;
import org.gradle.internal.impldep.org.codehaus.plexus.context.ContextException;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.AbstractLogEnabled;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.Logger;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.console.ConsoleLogger;
import org.gradle.internal.impldep.org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.gradle.internal.impldep.org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class AbstractArchiver extends AbstractLogEnabled implements Archiver, Contextualizable, FilterEnabled, FinalizerEnabled
{
    private Logger logger;
    private File destFile;
    private final List resources = new ArrayList();
    private boolean includeEmptyDirs = true;
    private int fileMode = -1;
    private int directoryMode = -1;
    private int defaultFileMode = -1;
    private int defaultDirectoryMode = -1;
    private boolean forced = true;
    private FilterSupport filterSupport;
    private List<ArchiveFinalizer> finalizers;
    private File dotFileDirectory;
    private String duplicateBehavior = "skip";
    private final boolean replacePathSlashesToJavaPaths;
    private boolean useJvmChmod;
    private ArchiverManager archiverManager;
    private boolean ignorePermissions;

    public AbstractArchiver() {
        this.replacePathSlashesToJavaPaths = File.separatorChar == '/';
        this.useJvmChmod = true;
        this.ignorePermissions = false;
    }

    public String getDuplicateBehavior() {
        return this.duplicateBehavior;
    }

    public void setDuplicateBehavior(String duplicate) {
        if (!Archiver.DUPLICATES_VALID_BEHAVIORS.contains(duplicate)) {
            throw new IllegalArgumentException("Invalid duplicate-file behavior: '" + duplicate + "'. Please specify one of: " + Archiver.DUPLICATES_VALID_BEHAVIORS);
        } else {
            this.duplicateBehavior = duplicate;
        }
    }

    public final void setFileMode(int mode) {
        if (mode >= 0) {
            this.fileMode = mode & 4095 | '耀';
        } else {
            this.fileMode = -1;
        }

    }

    public final void setDefaultFileMode(int mode) {
        this.defaultFileMode = mode & 4095 | '耀';
    }

    public final int getOverrideFileMode() {
        return this.fileMode;
    }

    public final int getFileMode() {
        if (this.fileMode < 0) {
            return this.defaultFileMode < 0 ? '膤' : this.defaultFileMode;
        } else {
            return this.fileMode;
        }
    }

    public final int getDefaultFileMode() {
        return this.defaultFileMode;
    }

    /** @deprecated */
    public final int getRawDefaultFileMode() {
        return this.getDefaultFileMode();
    }

    public final void setDirectoryMode(int mode) {
        if (mode >= 0) {
            this.directoryMode = mode & 4095 | 16384;
        } else {
            this.directoryMode = -1;
        }

    }

    public final void setDefaultDirectoryMode(int mode) {
        this.defaultDirectoryMode = mode & 4095 | 16384;
    }

    public final int getOverrideDirectoryMode() {
        return this.directoryMode;
    }

    public final int getDirectoryMode() {
        if (this.directoryMode < 0) {
            return this.defaultDirectoryMode < 0 ? 16877 : this.defaultDirectoryMode;
        } else {
            return this.directoryMode;
        }
    }

    public final int getDefaultDirectoryMode() {
        return this.defaultDirectoryMode < 0 ? 16877 : this.defaultDirectoryMode;
    }

    /** @deprecated */
    public final int getRawDefaultDirectoryMode() {
        return this.getDefaultDirectoryMode();
    }

    public boolean getIncludeEmptyDirs() {
        return this.includeEmptyDirs;
    }

    public void setIncludeEmptyDirs(boolean includeEmptyDirs) {
        this.includeEmptyDirs = includeEmptyDirs;
    }

    public void addDirectory(File directory) throws ArchiverException
    {
        this.addDirectory(directory, "");
    }

    public void addDirectory(File directory, String prefix) throws ArchiverException {
        this.addDirectory(directory, prefix, (String[])null, (String[])null);
    }

    public void addDirectory(File directory, String[] includes, String[] excludes) throws ArchiverException {
        this.addDirectory(directory, "", includes, excludes);
    }

    public void addDirectory(File directory, String prefix, String[] includes, String[] excludes) throws ArchiverException {
        DefaultFileSet fileSet = new DefaultFileSet();
        fileSet.setDirectory(directory);
        fileSet.setPrefix(prefix);
        fileSet.setIncludes(includes);
        fileSet.setExcludes(excludes);
        fileSet.setIncludingEmptyDirectories(this.includeEmptyDirs);
        this.addFileSet(fileSet);
    }

    public void addFileSet(FileSet fileSet) throws ArchiverException {
        File directory = fileSet.getDirectory();
        if (directory == null) {
            throw new ArchiverException("The file sets base directory is null.");
        } else if (!directory.isDirectory()) {
            throw new ArchiverException(directory.getAbsolutePath() + " isn't a directory.");
        } else {
            PlexusIoFileResourceCollection collection = new PlexusIoFileResourceCollection(this.getLogger());
            collection.setIncludes(fileSet.getIncludes());
            collection.setExcludes(fileSet.getExcludes());
            collection.setBaseDir(directory);
            collection.setFileSelectors(fileSet.getFileSelectors());
            collection.setIncludingEmptyDirectories(fileSet.isIncludingEmptyDirectories());
            collection.setPrefix(fileSet.getPrefix());
            collection.setCaseSensitive(fileSet.isCaseSensitive());
            collection.setUsingDefaultExcludes(fileSet.isUsingDefaultExcludes());
            if (this.getOverrideDirectoryMode() > -1 || this.getOverrideFileMode() > -1) {
                collection.setOverrideAttributes(-1, (String)null, -1, (String)null, this.getOverrideFileMode(), this.getOverrideDirectoryMode());
            }

            if (this.getDefaultDirectoryMode() > -1 || this.getDefaultFileMode() > -1) {
                collection.setDefaultAttributes(-1, (String)null, -1, (String)null, this.getDefaultFileMode(), this.getDefaultDirectoryMode());
            }

            this.addResources(collection);
        }
    }

    public void addFile(File inputFile, String destFileName) throws ArchiverException {
        int fileMode = this.getOverrideFileMode();
        this.addFile(inputFile, destFileName, fileMode);
    }

    protected ArchiveEntry asArchiveEntry(PlexusIoResource resource, String destFileName, int permissions) throws ArchiverException {
        if (!resource.isExisting()) {
            throw new ArchiverException(resource.getName() + " not found.");
        } else {
            return resource.isFile() ? ArchiveEntry.createFileEntry(destFileName, resource, permissions) : ArchiveEntry.createDirectoryEntry(destFileName, resource, permissions);
        }
    }

    protected ArchiveEntry asArchiveEntry(PlexusIoResourceCollection collection, PlexusIoResource resource) throws ArchiverException {
        try {
            String destFileName = collection.getName(resource);
            int permissions = -1;
            if (resource instanceof PlexusIoResourceWithAttributes) {
                PlexusIoResourceAttributes attrs = ((PlexusIoResourceWithAttributes)resource).getAttributes();
                if (attrs != null) {
                    permissions = attrs.getOctalMode();
                }
            }

            return this.asArchiveEntry(resource, destFileName, permissions);
        } catch (IOException var6) {
            throw new ArchiverException(var6.getMessage(), var6);
        }
    }

    public void addResource(PlexusIoResource resource, String destFileName, int permissions) throws ArchiverException {
        this.resources.add(this.asArchiveEntry(resource, destFileName, permissions));
    }

    public void addFile(File inputFile, String destFileName, int permissions) throws ArchiverException {
        if (inputFile.isFile() && inputFile.exists()) {
            InputStream in = null;
            if (this.replacePathSlashesToJavaPaths) {
                destFileName = destFileName.replace('\\', '/');
            }

            if (permissions < 0) {
                permissions = this.getOverrideFileMode();
            }

            try {
                if (this.filterSupport != null) {
                    in = new FileInputStream(inputFile);
                    if (this.include(in, destFileName)) {
                        this.resources.add(ArchiveEntry.createFileEntry(destFileName, inputFile, permissions));
                    }
                } else {
                    this.resources.add(ArchiveEntry.createFileEntry(destFileName, inputFile, permissions));
                }
            } catch (IOException var10) {
                throw new ArchiverException("Failed to determine inclusion status for: " + inputFile, var10);
            } catch (ArchiveFilterException var11) {
                throw new ArchiverException("Failed to determine inclusion status for: " + inputFile, var11);
            } finally {
                IOUtil.close(in);
            }

        } else {
            throw new ArchiverException(inputFile.getAbsolutePath() + " isn't a file.");
        }
    }

    public ResourceIterator getResources() throws ArchiverException {
        return new ResourceIterator() {
            private final Iterator addedResourceIter;
            private PlexusIoResourceCollection currentResourceCollection;
            private Iterator ioResourceIter;
            private ArchiveEntry nextEntry;
            private final Set<String> seenEntries;

            {
                this.addedResourceIter = AbstractArchiver.this.resources.iterator();
                this.seenEntries = new HashSet();
            }

            public boolean hasNext() {
                do {
                    if (this.nextEntry == null) {
                        if (this.ioResourceIter == null) {
                            if (this.addedResourceIter.hasNext()) {
                                Object o = this.addedResourceIter.next();
                                if (o instanceof ArchiveEntry) {
                                    this.nextEntry = (ArchiveEntry)o;
                                } else {
                                    if (!(o instanceof PlexusIoResourceCollection)) {
                                        return this.throwIllegalResourceType(o);
                                    }

                                    this.currentResourceCollection = (PlexusIoResourceCollection)o;

                                    try {
                                        this.ioResourceIter = this.currentResourceCollection.getResources();
                                    } catch (IOException var3) {
                                        throw new ArchiverException(var3.getMessage(), var3);
                                    }
                                }
                            } else {
                                this.nextEntry = null;
                            }
                        } else if (this.ioResourceIter.hasNext()) {
                            PlexusIoResource resource = (PlexusIoResource)this.ioResourceIter.next();
                            this.nextEntry = AbstractArchiver.this.asArchiveEntry(this.currentResourceCollection, resource);
                        } else {
                            this.ioResourceIter = null;
                        }
                    }

                    if (this.nextEntry != null && this.seenEntries.contains(this.nextEntry.getName())) {
                        String path = this.nextEntry.getName();
                        if (!"preserve".equals(AbstractArchiver.this.duplicateBehavior) && !"skip".equals(AbstractArchiver.this.duplicateBehavior)) {
                            if ("fail".equals(AbstractArchiver.this.duplicateBehavior)) {
                                throw new ArchiverException("Duplicate file " + path + " was found and the duplicate " + "attribute is 'fail'.");
                            }

                            AbstractArchiver.this.getLogger().debug("duplicate file " + path + " found, adding.");
                        } else {
                            if (this.nextEntry.getType() == 1) {
                                AbstractArchiver.this.getLogger().debug(path + " already added, skipping");
                            }

                            this.nextEntry = null;
                        }
                    }
                } while(this.nextEntry == null && (this.ioResourceIter != null || this.addedResourceIter.hasNext()));

                return this.nextEntry != null;
            }

            private boolean throwIllegalResourceType(Object o) {
                throw new IllegalStateException("An invalid resource of type: " + o.getClass().getName() + " was added to archiver: " + this.getClass().getName());
            }

            public ArchiveEntry next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    ArchiveEntry next = this.nextEntry;
                    this.nextEntry = null;
                    this.seenEntries.add(next.getName());
                    return next;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException("Does not support iterator");
            }
        };
    }

    public Map getFiles() {
        try {
            Map map = new HashMap();
            ResourceIterator iter = this.getResources();

            while(true) {
               ArchiveEntry entry;
                do {
                    if (!iter.hasNext()) {
                        return map;
                    }

                    entry = iter.next();
                } while(!this.includeEmptyDirs && entry.getType() != 1);

                map.put(entry.getName(), entry);
            }
        } catch (ArchiverException var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    public File getDestFile() {
        return this.destFile;
    }

    public void setDestFile(File destFile) {
        this.destFile = destFile;
        if (destFile != null) {
            destFile.getParentFile().mkdirs();
        }

    }

    protected Logger getLogger() {
        if (this.logger == null) {
            if (super.getLogger() != null) {
                this.logger = super.getLogger();
            } else {
                this.logger = new ConsoleLogger(1, "console");
            }
        }

        return this.logger;
    }

    public Map getDirs() {
        try {
            Map map = new HashMap();
            ResourceIterator iter = this.getResources();

            while(iter.hasNext()) {
                ArchiveEntry entry = iter.next();
                if (entry.getType() == 2) {
                    map.put(entry.getName(), entry);
                }
            }

            return map;
        } catch (ArchiverException var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    protected PlexusIoResourceCollection asResourceCollection(ArchivedFileSet fileSet) throws ArchiverException {
        File archiveFile = fileSet.getArchive();

        PlexusIoResourceCollection resources;
        try {
            resources = this.archiverManager.getResourceCollection(archiveFile);
        } catch (NoSuchArchiverException var5) {
            throw new ArchiverException("Error adding archived file-set. PlexusIoResourceCollection not found for: " + archiveFile, var5);
        }

        if (!(resources instanceof PlexusIoArchivedResourceCollection)) {
            throw new ArchiverException("Expected " + PlexusIoArchivedResourceCollection.class.getName() + ", got " + resources.getClass().getName());
        } else {
            ((PlexusIoArchivedResourceCollection)resources).setFile(fileSet.getArchive());
            PlexusIoProxyResourceCollection proxy = new PlexusIoProxyResourceCollection();
            proxy.setSrc(resources);
            proxy.setExcludes(fileSet.getExcludes());
            proxy.setIncludes(fileSet.getIncludes());
            proxy.setIncludingEmptyDirectories(fileSet.isIncludingEmptyDirectories());
            proxy.setCaseSensitive(fileSet.isCaseSensitive());
            proxy.setPrefix(fileSet.getPrefix());
            proxy.setUsingDefaultExcludes(fileSet.isUsingDefaultExcludes());
            proxy.setFileSelectors(fileSet.getFileSelectors());
            if (this.getOverrideDirectoryMode() > -1 || this.getOverrideFileMode() > -1) {
                proxy.setOverrideAttributes(-1, (String)null, -1, (String)null, this.getOverrideFileMode(), this.getOverrideDirectoryMode());
            }

            if (this.getDefaultDirectoryMode() > -1 || this.getDefaultFileMode() > -1) {
                proxy.setDefaultAttributes(-1, (String)null, -1, (String)null, this.getDefaultFileMode(), this.getDefaultDirectoryMode());
            }

            return proxy;
        }
    }

    public void addResources(PlexusIoResourceCollection collection) throws ArchiverException {
        this.resources.add(collection);
    }

    public void addArchivedFileSet(ArchivedFileSet fileSet) throws ArchiverException {
        PlexusIoResourceCollection resourceCollection = this.asResourceCollection(fileSet);
        this.addResources(resourceCollection);
    }

    public void addArchivedFileSet(File archiveFile, String prefix, String[] includes, String[] excludes) throws ArchiverException {
        DefaultArchivedFileSet fileSet = new DefaultArchivedFileSet();
        fileSet.setArchive(archiveFile);
        fileSet.setPrefix(prefix);
        fileSet.setIncludes(includes);
        fileSet.setExcludes(excludes);
        fileSet.setIncludingEmptyDirectories(this.includeEmptyDirs);
        this.addArchivedFileSet((ArchivedFileSet)fileSet);
    }

    public void addArchivedFileSet(File archiveFile, String prefix) throws ArchiverException {
        this.addArchivedFileSet(archiveFile, prefix, (String[])null, (String[])null);
    }

    public void addArchivedFileSet(File archiveFile, String[] includes, String[] excludes) throws ArchiverException {
        this.addArchivedFileSet(archiveFile, (String)null, includes, excludes);
    }

    public void addArchivedFileSet(File archiveFile) throws ArchiverException {
        this.addArchivedFileSet(archiveFile, (String)null, (String[])null, (String[])null);
    }

    public void contextualize(Context context) throws ContextException
    {
        PlexusContainer container = (PlexusContainer)context.get("plexus");

        try {
            this.archiverManager = (ArchiverManager)container.lookup(ArchiverManager.ROLE);
        } catch (ComponentLookupException var4) {
            throw new ContextException("Error retrieving ArchiverManager instance: " + var4.getMessage(), var4);
        }
    }

    public boolean isForced() {
        return this.forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public void setArchiveFilters(List filters) {
        this.filterSupport = new FilterSupport(filters, this.getLogger());
    }

    public void addArchiveFinalizer(ArchiveFinalizer finalizer) {
        if (this.finalizers == null) {
            this.finalizers = new ArrayList();
        }

        this.finalizers.add(finalizer);
    }

    public void setArchiveFinalizers(List<ArchiveFinalizer> archiveFinalizers) {
        this.finalizers = archiveFinalizers;
    }

    public void setDotFileDirectory(File dotFileDirectory) {
        this.dotFileDirectory = dotFileDirectory;
    }

    protected boolean isUptodate() throws ArchiverException {
        File zipFile = this.getDestFile();
        long destTimestamp = zipFile.lastModified();
        if (destTimestamp == 0L) {
            this.getLogger().debug("isUp2date: false (Destination " + zipFile.getPath() + " not found.)");
            return false;
        } else {
            Iterator it = this.resources.iterator();
            if (!it.hasNext()) {
                this.getLogger().debug("isUp2date: false (No input files.)");
                return false;
            } else {
                long l;
                do {
                    if (!it.hasNext()) {
                        this.getLogger().debug("isUp2date: true");
                        return true;
                    }

                    Object o = it.next();
                    if (o instanceof ArchiveEntry) {
                        l = ((ArchiveEntry)o).getResource().getLastModified();
                    } else {
                        if (!(o instanceof PlexusIoResourceCollection)) {
                            throw new IllegalStateException("Invalid object type: " + o.getClass().getName());
                        }

                        try {
                            l = ((PlexusIoResourceCollection)o).getLastModified();
                        } catch (IOException var9) {
                            throw new ArchiverException(var9.getMessage(), var9);
                        }
                    }

                    if (l == 0L) {
                        this.getLogger().debug("isUp2date: false (Resource with unknown modification date found.)");
                        return false;
                    }
                } while(l <= destTimestamp);

                this.getLogger().debug("isUp2date: false (Resource with newer modification date found.)");
                return false;
            }
        }
    }

    protected boolean checkForced() throws ArchiverException {
        if (!this.isForced() && this.isSupportingForced() && this.isUptodate()) {
            this.getLogger().debug("Archive " + this.getDestFile() + " is uptodate.");
            return false;
        } else {
            return true;
        }
    }

    public boolean isSupportingForced() {
        return false;
    }

    protected List getArchiveFinalizers() {
        return this.finalizers;
    }

    protected void runArchiveFinalizers() throws ArchiverException {
        if (this.finalizers != null) {
            Iterator it = this.finalizers.iterator();

            while(it.hasNext()) {
                ArchiveFinalizer finalizer = (ArchiveFinalizer)it.next();
                finalizer.finalizeArchiveCreation(this);
            }
        }

    }

    private boolean include(InputStream in, String path) throws ArchiveFilterException {
        return this.filterSupport == null || this.filterSupport.include(in, path);
    }

    public final void createArchive() throws ArchiverException, IOException {
        this.validate();

        try {
            try {
                if (this.dotFileDirectory != null) {
                    this.addArchiveFinalizer(new DotDirectiveArchiveFinalizer(this.dotFileDirectory));
                }

                this.runArchiveFinalizers();
                this.execute();
            } finally {
                this.close();
            }
        } catch (IOException var12) {
            String msg = "Problem creating " + this.getArchiveType() + ": " + var12.getMessage();
            StringBuffer revertBuffer = new StringBuffer();
            if (!this.revert(revertBuffer)) {
                msg = msg + revertBuffer.toString();
            }

            throw new ArchiverException(msg, var12);
        } finally {
            this.cleanUp();
        }

    }

    protected boolean hasVirtualFiles() {
        if (this.finalizers != null) {
            Iterator it = this.finalizers.iterator();

            while(it.hasNext()) {
                ArchiveFinalizer finalizer = (ArchiveFinalizer)it.next();
                List virtualFiles = finalizer.getVirtualFiles();
                if (virtualFiles != null && !virtualFiles.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean revert(StringBuffer messageBuffer) {
        return true;
    }

    protected void validate() throws ArchiverException, IOException {
    }

    protected abstract String getArchiveType();

    protected abstract void close() throws IOException;

    protected void cleanUp() {
        this.resources.clear();
    }

    protected abstract void execute() throws ArchiverException, IOException;

    public boolean isUseJvmChmod() {
        return this.useJvmChmod;
    }

    public void setUseJvmChmod(boolean useJvmChmod) {
        this.useJvmChmod = useJvmChmod;
    }

    public boolean isIgnorePermissions() {
        return this.ignorePermissions;
    }

    public void setIgnorePermissions(boolean ignorePermissions) {
        this.ignorePermissions = ignorePermissions;
    }
}
