//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.util;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.BaseFileSet;

public abstract class AbstractFileSet implements BaseFileSet
{
    private String prefix;
    private String[] includes;
    private String[] excludes;
    private FileSelector[] fileSelectors;
    private boolean caseSensitive = true;
    private boolean usingDefaultExcludes = true;
    private boolean includingEmptyDirectories = true;

    public AbstractFileSet() {
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
}
