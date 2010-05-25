/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
