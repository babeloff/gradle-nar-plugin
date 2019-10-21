//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver;

import org.gradle.internal.impldep.org.codehaus.plexus.util.FileUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DotDirectiveArchiveFinalizer extends AbstractArchiveFinalizer {
    private static String DEFAULT_DOT_FILE_PREFIX = ".plxarc";
    private File dotFileDirectory;
    private String dotFilePrefix;

    public DotDirectiveArchiveFinalizer(File dotFileDirectory) {
        this(dotFileDirectory, DEFAULT_DOT_FILE_PREFIX);
    }

    public DotDirectiveArchiveFinalizer(File dotFileDirectory, String dotFilePrefix) {
        this.dotFileDirectory = dotFileDirectory;
        this.dotFilePrefix = dotFilePrefix;
    }

    public void finalizeArchiveCreation(Archiver archiver) throws ArchiverException {
        try {
            List dotFiles = FileUtils.getFiles(this.dotFileDirectory, this.dotFilePrefix + "*", (String)null);
            Iterator i = dotFiles.iterator();

            while(i.hasNext()) {
                File dotFile = (File)i.next();
                BufferedReader in = new BufferedReader(new FileReader(dotFile));

                String line;
                while((line = in.readLine()) != null) {
                    String[] s = StringUtils.split(line, ":");
                    File directory;
                    if (s.length == 1) {
                        directory = new File(this.dotFileDirectory, s[0]);
                        System.out.println("adding directory = " + directory);
                        archiver.addDirectory(directory);
                    } else {
                        directory = new File(this.dotFileDirectory, s[0]);
                        System.out.println("adding directory = " + directory + " to: " + s[1]);
                        if (s[1].endsWith("/")) {
                            archiver.addDirectory(directory, s[1]);
                        } else {
                            archiver.addDirectory(directory, s[1] + "/");
                        }
                    }
                }

                in.close();
            }

        } catch (IOException var9) {
            throw new ArchiverException("Error processing dot files.", var9);
        }
    }

    public List getVirtualFiles() {
        return Collections.EMPTY_LIST;
    }
}
