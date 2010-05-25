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
package org.sonatype.flexmojos.truster;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.MavenMojo;

/**
 * Write a file entry onto flashplayer trust file
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.3
 * @goal trust
 * @phase process-resources
 */
public class TrusterMojo
    extends AbstractMojo
    implements MavenMojo
{

    /**
     * LW : needed for expression evaluation The maven MojoExecution needed for ExpressionEvaluation
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession context;

    /**
     * Files that should be included on flashplayer trust
     * 
     * @parameter
     * @required
     */
    private File[] filesToTrust;

    /**
     * @component
     */
    private FlashPlayerTruster truster;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        for ( File fileToTrust : filesToTrust )
        {
            try
            {
                truster.updateSecuritySandbox( fileToTrust );
            }
            catch ( TrustException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
    }

    public MavenSession getSession()
    {
        return context;
    }

}
