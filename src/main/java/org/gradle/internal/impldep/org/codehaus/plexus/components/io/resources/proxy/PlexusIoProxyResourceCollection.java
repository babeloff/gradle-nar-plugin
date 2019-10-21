//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.proxy;

import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributeUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes.SimpleResourceAttributes;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlexusIoProxyResourceCollection extends AbstractPlexusIoResourceCollectionWithAttributes
{
    private PlexusIoResourceCollection src;

    public PlexusIoProxyResourceCollection() {
    }

    public void setSrc(PlexusIoResourceCollection src) {
        this.src = src;
    }

    public PlexusIoResourceCollection getSrc() {
        return this.src;
    }

    public void setDefaultAttributes(int uid, String userName, int gid, String groupName, int fileMode, int dirMode) {
        this.setDefaultFileAttributes(new SimpleResourceAttributes(uid, userName, gid, groupName, fileMode));
        this.setDefaultDirAttributes(new SimpleResourceAttributes(uid, userName, gid, groupName, dirMode));
    }

    public void setOverrideAttributes(int uid, String userName, int gid, String groupName, int fileMode, int dirMode) {
        this.setOverrideFileAttributes(new SimpleResourceAttributes(uid, userName, gid, groupName, fileMode));
        this.setOverrideDirAttributes(new SimpleResourceAttributes(uid, userName, gid, groupName, dirMode));
    }

    protected FileSelector getDefaultFileSelector() {
        IncludeExcludeFileSelector fileSelector = new IncludeExcludeFileSelector();
        fileSelector.setIncludes(this.getIncludes());
        fileSelector.setExcludes(this.getExcludes());
        fileSelector.setCaseSensitive(this.isCaseSensitive());
        fileSelector.setUseDefaultExcludes(this.isUsingDefaultExcludes());
        return fileSelector;
    }

    public Iterator<PlexusIoResource> getResources() throws IOException {
        List<PlexusIoResource> result = new ArrayList();
        FileSelector fileSelector = this.getDefaultFileSelector();
        String prefix = this.getPrefix();
        if (prefix != null && prefix.length() == 0) {
            prefix = null;
        }

        Iterator iter = this.getSrc().getResources();

        while(true) {
            Object plexusIoResource;
            PlexusIoResourceAttributes attrs;
            do {
                do {
                    do {
                        if (!iter.hasNext()) {
                            return result.iterator();
                        }

                        plexusIoResource = (PlexusIoResource)iter.next();
                        attrs = null;
                        if (plexusIoResource instanceof PlexusIoResourceWithAttributes) {
                            attrs = ((PlexusIoResourceWithAttributes)plexusIoResource).getAttributes();
                        }

                        if (((PlexusIoResource)plexusIoResource).isDirectory()) {
                            attrs = PlexusIoResourceAttributeUtils.mergeAttributes(this.getOverrideDirAttributes(), attrs, this.getDefaultDirAttributes());
                        } else {
                            attrs = PlexusIoResourceAttributeUtils.mergeAttributes(this.getOverrideFileAttributes(), attrs, this.getDefaultFileAttributes());
                        }
                    } while(!fileSelector.isSelected((FileInfo)plexusIoResource));
                } while(!this.isSelected((PlexusIoResource)plexusIoResource));
            } while(((PlexusIoResource)plexusIoResource).isDirectory() && !this.isIncludingEmptyDirectories());

            if (prefix != null) {
                String name = ((PlexusIoResource)plexusIoResource).getName();
                if (plexusIoResource instanceof PlexusIoResourceWithAttributes) {
                    plexusIoResource = new PlexusIoProxyResourceWithAttributes((PlexusIoResource)plexusIoResource, attrs);
                } else {
                    plexusIoResource = new PlexusIoProxyResourceWithAttributes((PlexusIoResource)plexusIoResource, attrs);
                }

                ((AbstractPlexusIoResource)plexusIoResource).setName(prefix + name);
            }

            result.add((PlexusIoResource) plexusIoResource);
        }
    }

    public String getName(PlexusIoResource resource) throws IOException {
        String name = resource.getName();
        FileMapper[] mappers = this.getFileMappers();
        if (mappers != null) {
            FileMapper[] arr$ = mappers;
            int len$ = mappers.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                FileMapper mapper = arr$[i$];
                name = mapper.getMappedFileName(name);
            }
        }

        return name;
    }

    public long getLastModified() throws IOException {
        return this.src.getLastModified();
    }
}
