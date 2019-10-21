//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.util;

import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiveFileFilter;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiveFilterException;
import org.gradle.internal.impldep.org.codehaus.plexus.logging.Logger;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/** @deprecated */
public class FilterSupport {
    private final List filters;
    private final Logger logger;

    public FilterSupport(List filters, Logger logger) {
        this.filters = filters;
        this.logger = logger;
    }

    public boolean include(InputStream dataStream, String entryName) throws ArchiveFilterException
    {
        boolean included = true;
        if (this.filters != null && !this.filters.isEmpty()) {
            Iterator it = this.filters.iterator();

            while(it.hasNext()) {
                ArchiveFileFilter filter = (ArchiveFileFilter)it.next();
                included = filter.include(dataStream, entryName);
                if (!included) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Entry: '" + entryName + "' excluded by filter: " + filter.getClass().getName());
                    }
                    break;
                }
            }
        }

        return included;
    }
}
