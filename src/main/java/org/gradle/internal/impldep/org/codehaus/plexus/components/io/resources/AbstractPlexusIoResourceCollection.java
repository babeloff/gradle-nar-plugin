//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.filemappers.PrefixFileMapper;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.LogEnabled;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.Logger;

import java.io.IOException;
import java.util.Iterator;

public abstract class AbstractPlexusIoResourceCollection implements PlexusIoResourceCollection, LogEnabled
{
    private String prefix;
    private String[] includes;
    private String[] excludes;
    private FileSelector[] fileSelectors;
    private boolean caseSensitive = true;
    private boolean usingDefaultExcludes = true;
    private boolean includingEmptyDirectories = true;
    private FileMapper[] fileMappers;
    private Logger logger;

    protected AbstractPlexusIoResourceCollection() {
    }

    protected AbstractPlexusIoResourceCollection(Logger logger) {
        this.logger = logger;
    }

    protected Logger getLogger() {
        return this.logger;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public String[] getExcludes() {
        return this.excludes;
    }

    public void setFileSelectors(FileSelector[] fileSelectors) {
        this.fileSelectors = fileSelectors;
    }

    public FileSelector[] getFileSelectors() {
        return this.fileSelectors;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public String[] getIncludes() {
        return this.includes;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isCaseSensitive() {
        return this.caseSensitive;
    }

    public void setUsingDefaultExcludes(boolean usingDefaultExcludes) {
        this.usingDefaultExcludes = usingDefaultExcludes;
    }

    public boolean isUsingDefaultExcludes() {
        return this.usingDefaultExcludes;
    }

    public void setIncludingEmptyDirectories(boolean includingEmptyDirectories) {
        this.includingEmptyDirectories = includingEmptyDirectories;
    }

    public boolean isIncludingEmptyDirectories() {
        return this.includingEmptyDirectories;
    }

    protected boolean isSelected(PlexusIoResource plexusIoResource) throws IOException {
        FileSelector[] fileSelectors = this.getFileSelectors();
        if (fileSelectors != null) {
            for(int i = 0; i < fileSelectors.length; ++i) {
                if (!fileSelectors[i].isSelected(plexusIoResource)) {
                    return false;
                }
            }
        }

        return true;
    }

    public FileMapper[] getFileMappers() {
        return this.fileMappers;
    }

    public void setFileMappers(FileMapper[] fileMappers) {
        this.fileMappers = fileMappers;
    }

    public String getName(PlexusIoResource resource) throws IOException {
        String name = resource.getName();
        FileMapper[] mappers = this.getFileMappers();
        if (mappers != null) {
            for(int i = 0; i < mappers.length; ++i) {
                name = mappers[i].getMappedFileName(name);
            }
        }

        return PrefixFileMapper.getMappedFileName(this.getPrefix(), name);
    }

    public long getLastModified() throws IOException {
        long lastModified = 0L;
        Iterator iter = this.getResources();

        while(true) {
            long l;
            do {
                if (!iter.hasNext()) {
                    return lastModified;
                }

                PlexusIoResource res = (PlexusIoResource)iter.next();
                l = res.getLastModified();
                if (l == 0L) {
                    return 0L;
                }
            } while(lastModified != 0L && l <= lastModified);

            lastModified = l;
        }
    }

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
}
