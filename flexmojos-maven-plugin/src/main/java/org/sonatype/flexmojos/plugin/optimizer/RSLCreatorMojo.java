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
package org.sonatype.flexmojos.plugin.optimizer;

import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWF;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.compiler.IDigestConfiguration;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenRuntimeException;
import org.sonatype.flexmojos.test.util.PathUtil;

/**
 * Goal which run post-link SWF optimization on swc files. This goal is used to produce RSL files.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal create-rsl
 * @phase package
 */
public class RSLCreatorMojo
    extends AbstractOptimizerMojo
{

    /**
     * @component
     * @readonly
     */
    protected ArchiverManager archiverManager;

    /**
     * Optimized RSLs strip out any debugging information such as line numbers. This results in a smaller file, which
     * leads to shorter load times but makes it more difficult to read stacktrace errors as they contain no line
     * numbers.
     * <p>
     * Equivalent to optimizer execution
     * </p>
     * 
     * @parameter default-value="true" expression="${flex.optimizeRsl}"
     */
    private boolean optimizeRsl;

    /**
     * When true it does update the swc digester information, doesn't make any sense not do it
     * <p>
     * Equivalent to digester execution
     * </p>
     * 
     * @parameter expression="${flex.updateSwcDigest}"
     */
    private boolean updateSwcDigest;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().debug( "project.getPackaging = " + packaging );

        if ( !SWC.equals( packaging ) )
        {
            getLog().warn( "RSL creator mojo can only be used on SWC projects." );
            return;
        }

        File input = optimize();

        if ( updateSwcDigest )
        {
            int result;
            try
            {
                result = compiler.digest( getDigestConfiguration( input ), true ).getExitCode();
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }

            if ( result != 0 )
            {
                throw new MojoFailureException( "Got " + result + " errors building project, check logs" );
            }
        }

        getLog().debug( "attaching Artifact " );
        projectHelper.attachArtifact( project, SWF, new File( getOutput() ) );

    }

    protected File optimize( File input )
        throws MojoFailureException, MojoExecutionException
    {
        if ( optimizeRsl )
        {
            getLog().debug( "Optimizing" );
            final File output = new File( project.getBuild().getOutputDirectory(), "optimized.swf" );
            optimize( input, output );
            input = output;
        }
        return input;
    }

    public IDigestConfiguration getDigestConfiguration( final File input )
    {
        return new IDigestConfiguration()
        {

            public File getSwcPath()
            {
                return project.getArtifact().getFile();
            }

            public Boolean getSigned()
            {
                return false;
            }

            public File getRslFile()
            {
                return input;
            }
        };
    }

    @Override
    public String getInput()
    {
        File originalFile = project.getArtifact().getFile();

        getLog().debug( "attempting to optimize: " + originalFile.getName() );

        File bkpOriginalFile = new File( build.getDirectory(), build.getFinalName() + ".swf" );
        try
        {
            ZipFile zipFile = new ZipFile( originalFile );
            ZipEntry entry = zipFile.getEntry( "library.swf" );
            if ( entry == null )
            {
                throw new MavenRuntimeException( "Invalid SWC file. Library.swf not found. " + originalFile );
            }
            InputStream inputSWF = zipFile.getInputStream( entry );
            IOUtil.copy( inputSWF, new FileOutputStream( bkpOriginalFile ) );
        }
        catch ( Exception e )
        {
            throw new MavenRuntimeException( e.getMessage() + ": " + PathUtil.getCanonicalPath( originalFile ), e );
        }

        return PathUtil.getCanonicalPath( bkpOriginalFile );
    }

}
