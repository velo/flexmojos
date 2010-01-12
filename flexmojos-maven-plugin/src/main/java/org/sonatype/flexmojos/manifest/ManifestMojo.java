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
package org.sonatype.flexmojos.manifest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.sonatype.flexmojos.test.util.PathUtil;

/**
 * @phase generate-resources
 * @goal manifest
 * @author marvin
 */
public class ManifestMojo
    extends AbstractMojo
{

    /**
     * @parameter default-value="${project.compileSourceRoots}"
     * @readonly
     */
    private List<String> compileSourceRoots;

    /**
     * @parameter
     */
    private String[] manifestIncludes;

    /**
     * @parameter
     */
    private String[] manifestExcludes;

    /**
     * @parameter default-value="${project.build.directory}/manifest.xml"
     */
    private File outputFile;

    /**
     * @parameter default-value="${project.basedir}"
     * @readonly
     */
    private File basedir;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        List<String> classes = new ArrayList<String>();
        for ( String sourceRoot : compileSourceRoots )
        {
            File baseDir = new File( sourceRoot );
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setIncludes( addDefaultIncludes( manifestIncludes ) );
            scanner.setExcludes( manifestExcludes );
            scanner.addDefaultExcludes();
            scanner.setBasedir( baseDir );
            scanner.scan();

            classes.addAll( Arrays.asList( scanner.getIncludedFiles() ) );
        }

        Xpp3Dom dom = new Xpp3Dom( "componentPackage" );
        for ( String fileName : classes )
        {
            fileName = fileName.replace( ".as", "" ).replace( ".mxml", "" );

            String classname = fileName.replace( '/', '.' ).replace( '\\', '.' );
            String name = FilenameUtils.getExtension( classname );
            if ( StringUtils.isEmpty( name ) )
            {
                name = classname;
            }

            Xpp3Dom component = new Xpp3Dom( "component" );
            component.setAttribute( "id", name );
            component.setAttribute( "class", classname );
            dom.addChild( component );
        }

        String outputPath = PathUtil.getRelativePath( basedir, outputFile );
        getLog().info( "Writting " + classes.size() + " entry(ies) to manifest at: " + outputPath );
        Writer output = null;
        try
        {
            FileUtils.forceMkdir( outputFile.getParentFile() );
            output = new FileWriter( outputFile );
            IOUtil.copy( "<?xml version=\"1.0\"?>", output );
            Xpp3DomWriter.write( output, dom );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create outputFile at: " + outputFile, e );
        }
        finally
        {
            IOUtil.close( output );
        }
    }

    private String[] addDefaultIncludes( String[] manifestIncludes )
    {
        List<String> includes = new ArrayList<String>();
        if ( manifestIncludes != null )
        {
            includes.addAll( Arrays.asList( manifestIncludes ) );
        }

        includes.add( "**/*.as" );
        includes.add( "**/*.mxml" );

        return includes.toArray( new String[0] );
    }

}
