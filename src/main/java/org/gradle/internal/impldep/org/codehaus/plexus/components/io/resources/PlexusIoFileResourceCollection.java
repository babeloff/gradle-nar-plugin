//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.FileAttributes;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributeUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.Logger;
import org.gradle.internal.impldep.org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlexusIoFileResourceCollection extends AbstractPlexusIoResourceCollectionWithAttributes {
    public static final String ROLE_HINT = "files";
    private File baseDir;
    private boolean isFollowingSymLinks = true;

    public PlexusIoFileResourceCollection() {
    }

    public PlexusIoFileResourceCollection(Logger logger) {
        super(logger);
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getBaseDir() {
        return this.baseDir;
    }

    public boolean isFollowingSymLinks() {
        return this.isFollowingSymLinks;
    }

    public void setFollowingSymLinks(boolean pIsFollowingSymLinks) {
        this.isFollowingSymLinks = pIsFollowingSymLinks;
    }

    public void setDefaultAttributes(int uid, String userName, int gid, String groupName, int fileMode, int dirMode) {
        this.setDefaultFileAttributes(createResourceAttributes(uid, userName, gid, groupName, fileMode));
        this.setDefaultDirAttributes(createResourceAttributes(uid, userName, gid, groupName, dirMode));
    }

    public void setOverrideAttributes(int uid, String userName, int gid, String groupName, int fileMode, int dirMode) {
        this.setOverrideFileAttributes(createResourceAttributes(uid, userName, gid, groupName, fileMode));
        this.setOverrideDirAttributes(createResourceAttributes(uid, userName, gid, groupName, dirMode));
    }

    private static PlexusIoResourceAttributes createResourceAttributes(int uid, String userName, int gid, String groupName, int mode) {
        FileAttributes fileAttributes = new FileAttributes(uid, userName, gid, groupName, new char[0]);
        if (mode >= 0) {
            fileAttributes.setOctalMode(mode);
        }

        return fileAttributes;
    }

    private void addResources(List<PlexusIoResource> list, String[] resources, Map<String, PlexusIoResourceAttributes> attributesByPath) throws IOException {
        File dir = this.getBaseDir();
        String[] arr$ = resources;
        int len$ = resources.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String name = arr$[i$];
            String sourceDir = name.replace('\\', '/');
            File f = new File(dir, sourceDir);
            PlexusIoResourceAttributes attrs = (PlexusIoResourceAttributes)attributesByPath.get(name.length() > 0 ? name : ".");
            if (attrs == null) {
                attrs = (PlexusIoResourceAttributes)attributesByPath.get(f.getAbsolutePath());
            }

            if (f.isDirectory()) {
                attrs = PlexusIoResourceAttributeUtils.mergeAttributes(this.getOverrideDirAttributes(), attrs, this.getDefaultDirAttributes());
            } else {
                attrs = PlexusIoResourceAttributeUtils.mergeAttributes(this.getOverrideFileAttributes(), attrs, this.getDefaultFileAttributes());
            }

            PlexusIoFileResource resource = new PlexusIoFileResource(f, name, attrs);
            if (this.isSelected(resource)) {
                list.add(resource);
            }
        }

    }

    public Iterator<PlexusIoResource> getResources() throws IOException {
        DirectoryScanner ds = new DirectoryScanner();
        File dir = this.getBaseDir();
        ds.setBasedir(dir);
        String[] inc = this.getIncludes();
        if (inc != null && inc.length > 0) {
            ds.setIncludes(inc);
        }

        String[] exc = this.getExcludes();
        if (exc != null && exc.length > 0) {
            ds.setExcludes(exc);
        }

        if (this.isUsingDefaultExcludes()) {
            ds.addDefaultExcludes();
        }

        ds.setCaseSensitive(this.isCaseSensitive());
        ds.setFollowSymlinks(this.isFollowingSymLinks());
        ds.scan();
        Map<String, PlexusIoResourceAttributes> attributesByPath = PlexusIoResourceAttributeUtils.getFileAttributesByPath(this.getBaseDir());
        List<PlexusIoResource> result = new ArrayList();
        String[] files;
        if (this.isIncludingEmptyDirectories()) {
            files = ds.getIncludedDirectories();
            this.addResources(result, files, attributesByPath);
        }

        files = ds.getIncludedFiles();
        this.addResources(result, files, attributesByPath);
        return result.iterator();
    }
}
