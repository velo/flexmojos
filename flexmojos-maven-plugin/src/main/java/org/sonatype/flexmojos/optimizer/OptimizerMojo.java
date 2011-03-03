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
package org.sonatype.flexmojos.optimizer;

import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.MavenMojo;

import flex2.compiler.common.CompilerConfiguration;
import flex2.compiler.common.Configuration;
import flex2.compiler.mxml.lang.StandardDefs;
import flex2.compiler.swc.Digest;
import flex2.compiler.swc.Swc;
import flex2.compiler.swc.SwcCache;
import flex2.compiler.swc.SwcGroup;

/**
 * Goal which run post-link SWF optimization on swc files. This goal is used to produce RSL files.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @author Marla Bonar (Marla_Bonar@Intuit.com) - added ability to optimize a swf
 * @author Roberto Lo Giacco (rlogiacco@gmail.com) - fixed a few things regarding RSLs
 * @author Christopher Joseph Walzl (cwalzl@gmail.com) - added classifier awareness
 * @since 2.0
 * @goal optimize
 * @phase package
 */
public class OptimizerMojo
    extends AbstractMojo
    implements MavenMojo
{

    /**
     * @parameter expression="${project.build}"
     * @required
     * @readonly
     */
    protected Build build;

    /**
     * LW : needed for expression evaluation The maven MojoExecution needed for ExpressionEvaluation
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession context;

	/**
	 * Keep the following AS3 metadata in the bytecodes.<BR>
	 * Usage:
	 * 
	 * <pre>
	 * &lt;keepAs3Metadatas&gt;
	 *   &lt;keepAs3Metadata&gt;Bindable&lt;/keepAs3Metadata&gt;
	 *   &lt;keepAs3Metadata&gt;Events&lt;/keepAs3Metadata&gt;
	 * &lt;/keepAs3Metadatas&gt;
	 * </pre>
	 * 
	 * @parameter
	 */
	protected String[] keepAs3Metadatas;

	/**
	 * Optimized RSLs strip out any debugging information such as line numbers.
	 * This results in a smaller file, which leads to shorter load times but
	 * makes it more difficult to read stacktrace errors as they contain no line
	 * numbers.
     * 
     * @parameter expression="${optimizeRsls}" default-value="true"
     */
    protected boolean optimizeRsls;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     * 
     * @parameter expression="${flexmojos.classifier}"
     */
    protected String classifier;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    private File backupOriginalSWF( File originalFile )
        throws MojoExecutionException
    {
        File bkpOriginalFile = new File( build.getOutputDirectory(), "orig-library.swf" );
        try
        {
            FileUtils.copyFile( originalFile, bkpOriginalFile );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to backup SWF file.", e );
        }

        getLog().debug( "attaching original swf" );
        projectHelper.attachArtifact( project, SWF, "original", bkpOriginalFile );
        return bkpOriginalFile;
    }

    private Digest computeDigest( File optimizedSwf )
        throws MojoExecutionException
    {
        getLog().debug( "Computing optimized swf digest" );
        InputStream input = null;
        byte[] bytes;
        try
        {
            input = new FileInputStream( optimizedSwf );
            bytes = IOUtil.toByteArray( input );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error reading optimized SWF", e );
        }
        finally
        {
            IOUtil.close( input );
        }

        Digest d = new Digest();
        getLog().debug( d.computeDigest( bytes ) );
        return d;
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        String packaging = project.getPackaging();
        getLog().debug( "project.getPackaging = " + packaging );

        if ( !SWC.equals( packaging ) && !SWF.equals( packaging ) )
        {
            getLog().warn( "Optimizer mojo can only be used on SWC or SWF projects." );
            return;
        }

        String c = this.classifier == null ? "" : "-" + this.classifier;
        File output = new File( build.getDirectory(), build.getFinalName() + c + "." + packaging );

        if ( output == null || !output.exists() )
        {
            throw new MojoExecutionException( "Library file not found." );
        }

        getLog().debug( "Attempting to optimize: " + output.getName() );
        InputStream inputSWF = null;
        ZipFile zipFile = null;
        OutputStream outputSWF = null;
        try
        {
            if ( SWF.equals( packaging ) )
            {
                inputSWF = new FileInputStream( backupOriginalSWF( output ) );
                zipFile = null;
            }
            else
            {
                zipFile = newZipFile( output );
                inputSWF = readLibrarySwf( output, zipFile );
            }

            File optimizedSWFFile;
            if ( SWF.equals( packaging ) )
            {
                output.delete();
                optimizedSWFFile = output;
            }
            else
            {
                optimizedSWFFile = new File( build.getDirectory(), build.getFinalName() + c + "." + SWF );
            }

            optimizedSWFFile.getParentFile().mkdirs();
            outputSWF = new FileOutputStream( optimizedSWFFile );

            getLog().info( "Original file is: " + output.length() / 1024 + " k bytes" );
            if ( optimizeRsls )
            {
                optimize( inputSWF, outputSWF );
                getLog().info( "Optimized rsl is: " + optimizedSWFFile.length() / 1024 + " k bytes" );
                
                if ( SWC.equals( packaging ) )
                {
                    getLog().debug( "Updating digest " );
                    updateDigest( optimizedSWFFile, output );
                }
            }
            else
            {
                IOUtil.copy( inputSWF, outputSWF );
            }
            
            if ( SWC.equals( packaging ) )
            {
                getLog().debug( "Attaching swf artifact " );
                if ( classifier != null )
                {
                    projectHelper.attachArtifact( project, SWF, classifier, optimizedSWFFile );
                }
                else
                {
                    projectHelper.attachArtifact( project, SWF, optimizedSWFFile );
                }
            }
        }
        catch ( FileNotFoundException e )
        {
            // don't expect that
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( inputSWF );
            IOUtil.close( outputSWF );
            if ( zipFile != null )
            {
                try
                {
                    zipFile.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }

    public MavenSession getSession()
    {
        return context;
    }

    protected ZipFile newZipFile( File originalFile )
        throws MojoExecutionException
    {
        ZipFile zipFile = null;
        try
        {
            zipFile = new ZipFile( originalFile );
        }
        catch ( ZipException e )
        {
            throw new MojoExecutionException( "Invalid SWC file, is not a valid ZIP file.", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to read SWC file. " + originalFile, e );
        }
        return zipFile;
    }

    protected void optimize( InputStream inputSWF, OutputStream outputSWF )
        throws MojoExecutionException
    {
        try
        {
            Class<?> apiClass;
            try
            {
                apiClass = Class.forName( "flex2.tools.WebTierAPI" );
            }
            catch ( ClassNotFoundException e )
            {
                apiClass = Class.forName( "flex2.tools.API" );
            }

			Configuration configuration = new Configuration();
			configuration.setKeepDebugOpcodes( false );
			CompilerConfiguration compilerConfiguration = configuration.getCompilerConfiguration();
			compilerConfiguration.setOptimize( true );

			if ( keepAs3Metadatas != null )
			{
				compilerConfiguration.cfgKeepAs3Metadata( null, keepAs3Metadatas );
			}
			else
			{
				compilerConfiguration.cfgKeepAs3Metadata( null, StandardDefs.DefaultAS3Metadata );
			}

			Method optimizeMethod = apiClass.getDeclaredMethod( "optimize", InputStream.class, OutputStream.class,
					Configuration.class );
			optimizeMethod.invoke( null, inputSWF, outputSWF, configuration );
            outputSWF.flush();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "An error happen while trying to optimize.", e );
        }
    }

    protected InputStream readLibrarySwf( File originalFile, ZipFile zipFile )
        throws MojoFailureException, MojoExecutionException
    {
        InputStream inputSWF;
        ZipEntry entry = zipFile.getEntry( "library.swf" );
        if ( entry == null )
        {
            throw new MojoFailureException( "Invalid SWC file. Library.swf not found. " + originalFile );
        }
        try
        {
            inputSWF = zipFile.getInputStream( entry );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to read library.swf entry!", e );
        }
        return inputSWF;
    }

    @SuppressWarnings( "unchecked" )
	protected void updateDigest( File optimizedSWF, File originalFile )
        throws MojoExecutionException
    {
        Digest digest = computeDigest( optimizedSWF );
        getLog().debug( "Updating digest on " + originalFile );
        SwcCache swcCache = new SwcCache();
        String[] paths = { originalFile.getAbsolutePath() };
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
        finally
        {
            swc.close();
        }

    }

}
