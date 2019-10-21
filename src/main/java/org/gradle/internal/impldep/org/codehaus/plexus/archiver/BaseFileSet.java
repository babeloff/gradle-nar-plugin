//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors.FileSelector;

public interface BaseFileSet {
    String getPrefix();

    String[] getIncludes();

    String[] getExcludes();

    boolean isCaseSensitive();

    boolean isUsingDefaultExcludes();

    boolean isIncludingEmptyDirectories();

    FileSelector[] getFileSelectors();
}
