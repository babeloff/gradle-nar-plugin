//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors;

import java.io.IOException;

public interface FileSelector {
    String ROLE = FileSelector.class.getName();
    String DEFAULT_ROLE_HINT = "default";

    boolean isSelected(FileInfo var1) throws IOException;
}
