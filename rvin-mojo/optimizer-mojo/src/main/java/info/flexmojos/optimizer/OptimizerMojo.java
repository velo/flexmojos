/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.optimizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import eu.cedarsoft.utils.ZipExtractor;
import flex2.compiler.swc.Digest;
import flex2.compiler.swc.Swc;
import flex2.compiler.swc.SwcCache;
import flex2.compiler.swc.SwcGroup;

/**
 * Goal which optimize swc files.
 * 
 * @goal optimize
 * @phase package
 */
public class OptimizerMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter expression="${project.build}"
     * @required
     * @readonly
     */
    protected Build build;

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !"swc".equals( project.getPackaging() ) )
        {
            getLog().warn( "Optimizer mojo can only be used on SWC projects." );
            return;
        }

        File library = project.getArtifact().getFile();
        if ( library == null || !library.exists() )
        {
            throw new MojoExecutionException( "Library file not found." );
        }
        File librarySWF = extractLibrarySWF( library );
        File optimizedSWF = optimizeLibrarySWF( librarySWF );
        Digest digest = computeDigest( optimizedSWF );
        updateDigest( digest, library );

        projectHelper.attachArtifact( project, "swf", optimizedSWF );
    }

    @SuppressWarnings( "unchecked" )
    private void updateDigest( Digest digest, File library )
        throws MojoExecutionException
    {
        getLog().info( "Updating digest on " + project.getArtifact().getFile().getName() );
        SwcCache swcCache = new SwcCache();
        String[] paths = { library.getAbsolutePath() };
        SwcGroup group = swcCache.getSwcGroup( paths );
        Collection<Swc> swcs = group.getSwcs().values();
        Swc swc = swcs.iterator().next();
        swc.setDigest( Swc.LIBRARY_SWF, digest );

        // export SWC
        try
        {
            swcCache.export( swc );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to update digest information.", e );
        }
    }

    private Digest computeDigest( File swf )
        throws MojoExecutionException
    {
        getLog().info( "Computing optimized swf digest" );
        try
        {
            FileInputStream input = new FileInputStream( swf );
            byte[] bytes = new byte[(int) swf.length()];
            input.read( bytes );
            Digest d = new Digest();
            getLog().debug( d.computeDigest( bytes ) );
            return d;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "", e );
        }
    }

    private File optimizeLibrarySWF( File librarySWF )
        throws MojoExecutionException
    {
        getLog().info( "Optimizing library.swc" );
        File optimizedSWF = new File( build.getDirectory(), build.getFinalName() + ".swf" );

        try
        {
            InputStream input = new FileInputStream( librarySWF );
            OutputStream output = new FileOutputStream( optimizedSWF );
            // optimize
            flex2.tools.API.optimize( input, output );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "An error happen while trying to optimize SWC.", e );
        }

        return optimizedSWF;
    }

    private File extractLibrarySWF( File library )
        throws MojoExecutionException
    {
        getLog().info( "Extracting library.swf" );
        File outputDir = new File( build.getOutputDirectory() );
        try
        {
            ZipExtractor ze = new ZipExtractor( library );
            ze.extract( outputDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "An error happens when trying to extract library.swf from your SWC.", e );
        }

        File libraryFile = new File( outputDir, "library.swf" );
        return libraryFile;
    }

}
