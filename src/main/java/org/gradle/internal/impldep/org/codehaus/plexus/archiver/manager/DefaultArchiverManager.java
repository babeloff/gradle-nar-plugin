package org.gradle.internal.impldep.org.codehaus.plexus.archiver.manager;

/*
 * Copyright  2001,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


import org.gradle.internal.impldep.org.codehaus.plexus.PlexusConstants;
import org.gradle.internal.impldep.org.codehaus.plexus.PlexusContainer;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.Archiver;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.UnArchiver;
import org.gradle.internal.impldep.org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.gradle.internal.impldep.org.codehaus.plexus.components.io.resources.PlexusIoResourceCollection;
import org.gradle.internal.impldep.org.codehaus.plexus.context.Context;
import org.gradle.internal.impldep.org.codehaus.plexus.context.ContextException;
import org.gradle.internal.impldep.org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.gradle.internal.impldep.org.codehaus.plexus.util.FileUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * @author dantran
 * @version $Revision:
 */

public class DefaultArchiverManager
        implements ArchiverManager, Contextualizable
{
    private PlexusContainer container;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void contextualize( Context context )
            throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    @Nonnull public Archiver getArchiver(@Nonnull String archiverName )
            throws NoSuchArchiverException
    {
        try
        {
            return (Archiver) container.lookup( Archiver.ROLE, archiverName );
        }
        catch ( ComponentLookupException e )
        {
            throw new NoSuchArchiverException( archiverName );
        }
    }

    @Nonnull public UnArchiver getUnArchiver(@Nonnull String unArchiverName )
            throws NoSuchArchiverException
    {
        try
        {
            return (UnArchiver) container.lookup( UnArchiver.ROLE, unArchiverName );
        }
        catch ( ComponentLookupException e )
        {
            throw new NoSuchArchiverException( unArchiverName );
        }
    }


    public @Nonnull
    PlexusIoResourceCollection getResourceCollection(String resourceCollectionName )
            throws NoSuchArchiverException
    {
        try
        {
            return (PlexusIoResourceCollection) container.lookup( PlexusIoResourceCollection.ROLE, resourceCollectionName );
        }
        catch ( ComponentLookupException e )
        {
            throw new NoSuchArchiverException( resourceCollectionName );
        }
    }

    private static @Nonnull String getFileExtention ( @Nonnull File file )
    {
        String path = file.getAbsolutePath();

        String archiveExt = FileUtils.getExtension( path ).toLowerCase( Locale.ENGLISH );

        if ( "gz".equals( archiveExt ) || "bz2".equals( archiveExt ) )
        {
            String [] tokens = StringUtils.split( path, "." );

            if ( tokens.length > 2  && "tar".equals( tokens[tokens.length -2].toLowerCase( Locale.ENGLISH ) ) )
            {
                archiveExt = "tar." + archiveExt;
            }
        }

        return archiveExt;

    }
    @Nonnull public Archiver getArchiver( @Nonnull File file )
            throws NoSuchArchiverException
    {
        return getArchiver( getFileExtention( file ) );
    }

    @Nonnull public UnArchiver getUnArchiver( @Nonnull File file )
            throws NoSuchArchiverException
    {
        return getUnArchiver( getFileExtention( file ) );
    }

    @Nonnull public PlexusIoResourceCollection getResourceCollection( @Nonnull File file )
            throws NoSuchArchiverException
    {
        return getResourceCollection( getFileExtention( file ) );
    }
}