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
