/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.optimizer;

import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.common.MavenRuntimeException;
import org.sonatype.flexmojos.compiler.IDigestConfiguration;
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
    implements IDigestConfiguration
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

        if ( optimizeRsl )
        {
            try
            {
                compiler.optimize( getOptimizerConfiguration() );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }

        if ( updateSwcDigest )
        {
            try
            {
                compiler.digest( getDigestConfiguration() );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }

        getLog().debug( "attaching Artifact " );
        projectHelper.attachArtifact( project, SWF, new File( getOutput() ) );

    }

    public IDigestConfiguration getDigestConfiguration()
    {
        return this;
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
            throw new MavenRuntimeException( e.getMessage(), e );
        }

        return PathUtil.getCanonicalPath( bkpOriginalFile );
    }

    public File getRslFile()
    {
        return new File( getOutput() );
    }

    public Boolean getSigned()
    {
        return false;
    }

    public File getSwcPath()
    {
        return project.getArtifact().getFile();
    }

}
