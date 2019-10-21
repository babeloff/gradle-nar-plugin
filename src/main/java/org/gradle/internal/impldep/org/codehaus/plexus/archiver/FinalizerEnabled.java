//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import java.util.List;

public interface FinalizerEnabled {
    void addArchiveFinalizer(ArchiveFinalizer var1);

    void setArchiveFinalizers(List<ArchiveFinalizer> var1);
}
