/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.install;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.components.publisher.FlexSDKPublisher;
import org.sonatype.flexmojos.components.publisher.PublishingException;

public abstract class AbstractInstallMojo
    extends AbstractMojo
{

    /**
     * File location where targeted Flex SDK is located
     * 
     * @parameter expression="${flex.sdk.folder}"
     * @required
     */
    protected File sdkFolder;

    /**
     * Flex SDK version. Recommend pattern:
     * <ul>
     * Append -FB3 suffix on Flexbuilder sdks
     * </ul>
     * <ul>
     * Append -LCDS suffix on LCDS sdks
     * </ul>
     * <BR>
     * Samples:
     * <ul>
     * 3.0.0.477
     * </ul>
     * <ul>
     * 3.0.0.477-FB3
     * </ul>
     * <ul>
     * 3.0.0.477-LCDS
     * </ul>
     * 
     * @parameter expression="${version}"
     * @required
     */
    protected String version;

    /**
     * @parameter expression="${overwriteLibFolder}"
     */
    protected File overwriteLibFolder;

    /**
     * @parameter expression="${default.player.version}" default-value="9"
     */
    protected int defaultPlayerVersion;

    public AbstractInstallMojo()
    {
        super();
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            getPublisher().publish( this.sdkFolder, this.version, this.defaultPlayerVersion, this.overwriteLibFolder );
        }
        catch ( PublishingException e )
        {
            throw new MojoFailureException( "Unable to install flex SDK", e );
        }
    }

    protected abstract FlexSDKPublisher getPublisher()
        throws MojoExecutionException;

}