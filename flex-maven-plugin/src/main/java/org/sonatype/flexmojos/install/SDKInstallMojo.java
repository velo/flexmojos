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
import org.sonatype.flexmojos.components.publisher.FlexSDKPublisher;

/**
 * @goal install-sdk
 * @requiresProject false
 * @requiresDirectInvocation true
 * @author marvin
 */
public class SDKInstallMojo
    extends AbstractInstallMojo
{

    /**
     * @component role="org.sonatype.flexmojos.components.publisher.FlexSDKPublisher" roleHint="install"
     */
    private FlexSDKPublisher publisher;

    @Override
    protected FlexSDKPublisher getPublisher()
        throws MojoExecutionException
    {
        context.put( "localRepository", localRepository );

        return publisher;
    }

}
