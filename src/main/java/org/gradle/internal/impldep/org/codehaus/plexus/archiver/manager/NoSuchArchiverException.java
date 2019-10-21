//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.manager;

public class NoSuchArchiverException extends Exception {
    private String archiverName;

    public NoSuchArchiverException(String archiverName) {
        super("No such archiver: '" + archiverName + "'.");
        this.archiverName = archiverName;
    }

    public String getArchiver() {
        return this.archiverName;
    }
}
