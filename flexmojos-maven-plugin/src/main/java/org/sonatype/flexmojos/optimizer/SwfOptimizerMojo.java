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

import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.common.MavenRuntimeException;
import org.sonatype.flexmojos.test.util.PathUtil;

/**
 * Goal which run post-link SWF optimization on swc files. This goal is used to produce RSL files.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 2.0
 * @goal optimize-swf
 * @phase package
 */
public class SwfOptimizerMojo
    extends AbstractOptimizerMojo
{

    /**
     * Defines whether the original artifact should be attached as classifier to the optimized artifact. If false, the
     * optimized swf will be the main artifact of the project
     * 
     * @parameter default-value="true" expression="${flex.attachOriginalArtifact}"
     */
    private boolean attachOriginalArtifact;

    /**
     * The name of the classifier used in case the original artifact is attached.
     * 
     * @parameter default-value="original" expression="${flex.originalClassifierName}"
     */
    private String originalClassifierName;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().debug( "project.getPackaging = " + packaging );

        if ( !SWF.equals( packaging ) )
        {
            getLog().warn( "Swf optimizer goal can only be used on SWF projects." );
            return;
        }

        try
        {
            compiler.optimize( getOptimizerConfiguration() , true);
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

    }

    @Override
    public String getInput()
    {
        File originalFile = project.getArtifact().getFile();

        getLog().debug( "attempting to optimize: " + originalFile.getName() );

        File bkpOriginalFile = getOriginalSwfFile();

        try
        {
            FileUtils.copyFile( originalFile, bkpOriginalFile );
        }
        catch ( IOException e )
        {
            throw new MavenRuntimeException( "Unable to backup SWF file.", e );
        }

        getLog().debug( "attaching original swf" );
        if ( attachOriginalArtifact )
        {
            projectHelper.attachArtifact( project, SWF, originalClassifierName, bkpOriginalFile );
        }

        return PathUtil.getCanonicalPath( bkpOriginalFile );
    }

    protected File getOriginalSwfFile()
    {
        File bkpOriginalFile =
            new File( build.getDirectory(), build.getFinalName() + "-" + originalClassifierName + ".swf" );
        return bkpOriginalFile;
    }
}
