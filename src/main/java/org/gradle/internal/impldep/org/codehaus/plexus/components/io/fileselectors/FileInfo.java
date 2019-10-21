//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors;

import java.io.IOException;
import java.io.InputStream;

public interface FileInfo {
    String getName();

    InputStream getContents() throws IOException;

    boolean isFile();

    boolean isDirectory();
}
