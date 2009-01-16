/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.install;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.flexmojos.components.publisher.FlexSDKPublisher;

/**
 * @goal install-sdk
 * @requiresProject false
 * @requiresDirectInvocation true
 * @author marvin
 */
public class SDKInstallMojo
    extends AbstractInstallMojo
    implements Contextualizable
{

    /**
     * @parameter default-value="${localRepository}"
     */
    private Object localRepository;

    private Context context;

    @Override
    protected FlexSDKPublisher getPublisher()
        throws MojoExecutionException
    {
        context.put( "localRepository", localRepository );

        FlexSDKPublisher publisher;
        try
        {
            PlexusContainer plexusContainer = (PlexusContainer) context.get( "plexus" );
            publisher =
                (FlexSDKPublisher) plexusContainer.lookup(
                                                           "org.sonatype.flexmojos.components.publisher.FlexSDKPublisher",
                                                           "install" );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException(
                                              "Unable to look for :org.sonatype.flexmojos.components.publisher.FlexSDKPublisher",
                                              e );
        }
        return publisher;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        this.context = context;
    }

}
