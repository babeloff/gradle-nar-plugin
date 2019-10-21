//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors;

import org.gradle.internal.impldep.org.codehaus.plexus.util.FileUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.util.SelectorUtils;

import java.io.File;
import java.io.IOException;

public class IncludeExcludeFileSelector implements FileSelector {
    public static final String ROLE_HINT = "standard";
    private static final String[] ALL_INCLUDES = new String[]{getCanonicalName("**/*")};
    private static final String[] ZERO_EXCLUDES = new String[0];
    private boolean isCaseSensitive = true;
    private boolean useDefaultExcludes = true;
    private String[] includes;
    private String[] excludes;
    private String[] computedIncludes;
    private String[] computedExcludes;

    public IncludeExcludeFileSelector() {
        this.computedIncludes = ALL_INCLUDES;
        this.computedExcludes = ZERO_EXCLUDES;
    }

    protected boolean isExcluded(String name) {
        for(int i = 0; i < this.computedExcludes.length; ++i) {
            if (this.matchPath(this.computedExcludes[i], name, this.isCaseSensitive)) {
                return true;
            }
        }

        return false;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
        if (includes == null) {
            this.computedIncludes = ALL_INCLUDES;
        } else {
            this.computedIncludes = new String[includes.length];

            for(int i = 0; i < includes.length; ++i) {
                String pattern = this.asPattern(includes[i]);
                this.computedIncludes[i] = pattern;
            }
        }

    }

    private static String getCanonicalName(String pName) {
        return pName.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    }

    private String asPattern(String pPattern) {
        String pattern = getCanonicalName(pPattern.trim());
        if (pattern.endsWith(File.separator)) {
            pattern = pattern + "**";
        }

        return pattern;
    }

    public String[] getIncludes() {
        return this.includes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
        String[] defaultExcludes = this.useDefaultExcludes ? FileUtils.getDefaultExcludes() : ZERO_EXCLUDES;
        if (excludes == null) {
            this.computedExcludes = defaultExcludes;
        } else {
            this.computedExcludes = new String[excludes.length + defaultExcludes.length];

            for(int i = 0; i < excludes.length; ++i) {
                this.computedExcludes[i] = this.asPattern(excludes[i]);
            }

            if (defaultExcludes.length > 0) {
                System.arraycopy(defaultExcludes, 0, this.computedExcludes, excludes.length, defaultExcludes.length);
            }
        }

    }

    public String[] getExcludes() {
        return this.excludes;
    }

    protected boolean matchPath(String pattern, String name, boolean isCaseSensitive) {
        return SelectorUtils.matchPath(pattern, name, isCaseSensitive);
    }

    protected boolean isIncluded(String name) {
        for(int i = 0; i < this.computedIncludes.length; ++i) {
            if (this.matchPath(this.computedIncludes[i], name, this.isCaseSensitive)) {
                return true;
            }
        }

        return false;
    }

    public boolean isSelected(FileInfo fileInfo) throws IOException {
        String name = getCanonicalName(fileInfo.getName());
        return this.isIncluded(name) && !this.isExcluded(name);
    }

    public boolean isCaseSensitive() {
        return this.isCaseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.isCaseSensitive = caseSensitive;
    }

    public boolean isUseDefaultExcludes() {
        return this.useDefaultExcludes;
    }

    public void setUseDefaultExcludes(boolean pUseDefaultExcludes) {
        this.useDefaultExcludes = pUseDefaultExcludes;
        this.setExcludes(this.excludes);
    }
}
