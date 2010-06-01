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
package org.sonatype.flexmojos.plugin.common;

import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;

public aspect UsageMonitor
{

    private static final String TRACKER = "flexmojos-JGoogleAnalyticsTracker";

    pointcut execute() : target(AbstractMavenMojo) && execution(void execute() );

    @SuppressWarnings( "unchecked" )
    before() : execute() 
    {
        final AbstractMavenMojo mojo = (AbstractMavenMojo) thisJoinPoint.getThis();

        JGoogleAnalyticsTracker tracker =
            (JGoogleAnalyticsTracker) mojo.getPluginContext().get( TRACKER );
        if ( tracker == null )
        {
            byte[] bytes = new byte[] { 85, 65, 45, 51, 57, 51, 57, 48, 55, 52, 45, 51 };
            tracker = new JGoogleAnalyticsTracker( "Flexmojos", MavenUtils.getFlexMojosVersion(), new String( bytes ) );
            mojo.getPluginContext().put( TRACKER, tracker );
        }
        

        FocusPoint focusPoint = new FocusPoint( mojo.getClass().getName() );

        // prevent some asynchronous issues related to new maven 3 parallel support
        if ( getClass().getPackage().getName().startsWith( new String( new byte[] { 111, 114, 103, 46, 115, 111, 110,
                                                               97, 116, 121, 112, 101, 46, 102, 108, 101, 120, 109,
                                                               111, 106, 111, 115 } ) ) )
        {
            tracker.trackAsynchronously( focusPoint );
        }
        else
        {
            tracker.trackSynchronously( focusPoint );
        }
    }

}
