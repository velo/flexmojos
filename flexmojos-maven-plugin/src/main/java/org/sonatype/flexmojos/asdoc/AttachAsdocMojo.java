/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonatype.flexmojos.asdoc;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * Goal which generates documentation from the ActionScript sources in DITA format.
 * 
 * @phase package
 * @goal attach-asdoc
 * @requiresDependencyResolution
 */
public class AttachAsdocMojo
    extends AsDocMojo
{

    /**
     * The filename of bundled asdoc
     * 
     * @parameter default-value="${project.build.directory}/${project.build.finalName}-asdoc.zip"
     */
    private File output;

    /**
     * @component
     */
    private ArchiverManager archiverManager;

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    @Override
    protected void tearDown()
        throws MojoExecutionException, MojoFailureException
    {
        super.tearDown();

        output.getParentFile().mkdirs();

        Archiver archiver;
        try
        {
            archiver = archiverManager.getArchiver( output );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Invalid file type", e );
        }
        try
        {
            archiver.addDirectory( outputDirectory );
            archiver.setDestFile( output );
            archiver.createArchive();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to create bundle", e );
        }

        projectHelper.attachArtifact( project, "zip", "asdoc", output );
    }

}
