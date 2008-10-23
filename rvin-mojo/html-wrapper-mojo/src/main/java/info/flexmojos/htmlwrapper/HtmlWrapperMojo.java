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
package info.flexmojos.htmlwrapper;

import info.rvin.flexmojos.utilities.CompileConfigurationLoader;
import info.rvin.flexmojos.utilities.MavenUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import eu.cedarsoft.utils.ZipExtractor;

/**
 * @phase generate-resources
 * @goal wrapper
 * @author marvin
 */
public class HtmlWrapperMojo
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
     * The template URI.
     * <p>
     * You can point to a zip file, a folder or use one of the following embed templates:
     * <ul>
     * embed:client-side-detection
     * </ul>
     * <ul>
     * embed:client-side-detection-with-history
     * </ul>
     * <ul>
     * embed:express-installation
     * </ul>
     * <ul>
     * embed:express-installation-with-history
     * </ul>
     * <ul>
     * embed:no-player-detection
     * </ul>
     * <ul>
     * embed:no-player-detection-with-history
     * </ul>
     * To point to a zip file you must use a URI like this:
     * 
     * <pre>
     * zip:/myTemplateFolder/template.zip
     * zip:c:/myTemplateFolder/template.zip
     * </pre>
     * 
     * To point to a folder use a URI like this:
     * 
     * <pre>
     * folder:/myTemplateFolder/
     * folder:c:/myTemplateFolder/
     * </pre>
     * <p>
     * This mojo will look for <tt>index.template.html</tt> for replace parameters
     * 
     * @parameter default-value="embed:client-side-detection-with-history"
     */
    private String templateURI;

    /**
     * Used to define parameters that will be replaced. Usage:
     * 
     * <pre>
     *  &lt;parameters&gt;
     *      &lt;swf&gt;${build.finalName}&lt;/swf&gt;
     *      &lt;width&gt;100%&lt;/width&gt;
     *      &lt;height&gt;100%&lt;/height&gt;
     *  &lt;/parameters&gt;
     * </pre>
     * 
     * The following prameters wil be injected if not defined:
     * <ul>
     * title
     * </ul>
     * <ul>
     * version_major
     * </ul>
     * <ul>
     * version_minor
     * </ul>
     * <ul>
     * version_revision
     * </ul>
     * <ul>
     * swf
     * </ul>
     * <ul>
     * width
     * </ul>
     * <ul>
     * height
     * </ul>
     * <ul>
     * bgcolor
     * </ul>
     * <ul>
     * application
     * </ul>
     * If you are using a custom template, and wanna some extra parameters, this is the right place to define it.
     * 
     * @parameter
     */
    private Map<String, String> parameters;

    /**
     * output Directory to store final html
     * 
     * @parameter default-value="${project.build.directory}"
     */
    private File outputDirectory;

    /**
     * final name of html file
     * 
     * @parameter default-value="${project.build.finalName}"
     */
    private String htmlName;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        init();

        File templateFolder = extractTemplate();
        copyTemplates( templateFolder );
        copyIndex( templateFolder );
    }

    private void copyIndex( File templateFolder )
        throws MojoExecutionException
    {
        File indexTemplate = new File( templateFolder, "index.template.html" );
        String template;
        try
        {
            template = FileUtils.readFileToString( indexTemplate );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to read " + indexTemplate, e );
        }

        for ( String key : parameters.keySet() )
        {
            String value = parameters.get( key );
            template = template.replace( "${" + key + "}", value );
        }

        File index = new File( outputDirectory, htmlName + ".html" );

        try
        {
            FileUtils.writeStringToFile( index, template );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to write " + index, e );
        }
    }

    private void copyTemplates( File templateFolder )
        throws MojoExecutionException
    {
        try
        {
            FileUtils.copyDirectory( templateFolder, outputDirectory, new FileFilter()
            {
                public boolean accept( File pathname )
                {
                    return !"index.template.html".equals( pathname.getName() );
                }
            } );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to create templates.", e );
        }
    }

    private File extractTemplate()
        throws MojoExecutionException
    {
        getLog().info( "Extracting template" );
        File outputDir = new File( build.getOutputDirectory(), "html-template" );
        outputDir.mkdirs();

        URI uri;
        try
        {
            if ( MavenUtils.isWindows() )
            {
                // Shake bars to avoid URI syntax problems
                templateURI = templateURI.replace( '\\', '/' );
            }
            templateURI = URIUtil.encodePath( templateURI );
            uri = new URI( templateURI );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Invalid template URI.", e );
        }

        String scheme = uri.getScheme();
        if ( "embed".equals( scheme ) )
        {
            copyEmbedTemplate( uri.getSchemeSpecificPart(), outputDir );
        }
        else if ( "zip".equals( scheme ) )
        {
            copyZipTemplate( uri.getSchemeSpecificPart(), outputDir );
        }
        else if ( "folder".equals( scheme ) )
        {
            copyFolderTemplate( uri.getSchemeSpecificPart(), outputDir );
        }
        else
        {
            throw new MojoExecutionException( "Invalid URI scheme: " + scheme );
        }

        return outputDir;
    }

    private void copyFolderTemplate( String path, File outputDir )
        throws MojoExecutionException
    {
        File source = new File( path );
        if ( !source.exists() || !source.isDirectory() )
        {
            throw new MojoExecutionException( "Template folder doesn't exists. " + source );
        }

        try
        {
            FileUtils.copyDirectory( source, outputDir,
                                     FileFilterUtils.makeSVNAware( FileFilterUtils.makeCVSAware( null ) ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy template to: " + outputDir, e );
        }
    }

    private void copyZipTemplate( String path, File outputDir )
        throws MojoExecutionException
    {
        File source = new File( path );
        if ( !source.exists() || !source.isFile() )
        {
            throw new MojoExecutionException( "Zip template doesn't exists. " + source );
        }

        extractZipTemplate( outputDir, source );
    }

    private void copyEmbedTemplate( String path, File outputDir )
        throws MojoExecutionException
    {
        URL url = getClass().getResource( "/" + path + ".zip" );
        File template = new File( build.getOutputDirectory(), "template.zip" );
        try
        {
            FileUtils.copyURLToFile( url, template );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy template to: " + template, e );
        }
        extractZipTemplate( outputDir, template );
    }

    private void extractZipTemplate( File outputDir, File template )
        throws MojoExecutionException
    {
        try
        {
            ZipExtractor ze = new ZipExtractor( template );
            ze.extract( outputDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "An error happens when trying to extract html-template.", e );
        }
    }

    private void init()
    {
        if ( parameters == null )
        {
            parameters = new HashMap<String, String>();
        }

        if ( !parameters.containsKey( "title" ) )
        {
            parameters.put( "title", project.getName() );
        }

        String targetPlayer = CompileConfigurationLoader.getCompilerPluginSetting( project, "targetPlayer" );
        String[] nodes = targetPlayer != null ? targetPlayer.split( "\\." ) : new String[] { "9", "0", "0" };
        if ( !parameters.containsKey( "version_major" ) )
        {
            parameters.put( "version_major", nodes[0] );
        }
        if ( !parameters.containsKey( "version_minor" ) )
        {
            parameters.put( "version_minor", nodes[1] );
        }
        if ( !parameters.containsKey( "version_revision" ) )
        {
            parameters.put( "version_revision", nodes[2] );
        }
        if ( !parameters.containsKey( "swf" ) )
        {
            parameters.put( "swf", project.getBuild().getFinalName() );
        }
        if ( !parameters.containsKey( "width" ) )
        {
            parameters.put( "width", "100%" );
        }
        if ( !parameters.containsKey( "height" ) )
        {
            parameters.put( "height", "100%" );
        }
        if ( !parameters.containsKey( "application" ) )
        {
            parameters.put( "application", project.getArtifactId() );
        }
        if ( !parameters.containsKey( "bgcolor" ) )
        {
            parameters.put( "bgcolor", "#869ca7" );
        }
    }

}
