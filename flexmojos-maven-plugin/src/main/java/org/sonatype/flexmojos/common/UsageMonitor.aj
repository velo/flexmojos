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
package org.sonatype.flexmojos.common;

import org.apache.maven.execution.MavenSession;
import org.sonatype.flexmojos.MavenMojo;
import org.sonatype.flexmojos.utilities.MavenUtils;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;

public aspect UsageMonitor
{

    pointcut execute() : target(MavenMojo) && execution(void execute() );

    before() : execute() 
    {
        final MavenMojo mojo = (MavenMojo) thisJoinPoint.getTarget();
        MavenSession s = mojo.getSession();

        Object tracker = s.getExecutionProperties().get( "JGoogleAnalyticsTracker" );
        if ( !( tracker instanceof JGoogleAnalyticsTracker ) )
        {
            byte[] bytes = new byte[] { 85, 65, 45, 51, 57, 51, 57, 48, 55, 52, 45, 51 };
            tracker = new JGoogleAnalyticsTracker( "Flexmojos", MavenUtils.getFlexMojosVersion(), new String( bytes ) );
            s.getExecutionProperties().put( "JGoogleAnalyticsTracker", tracker );
        }

        FocusPoint focusPoint = new FocusPoint( mojo.getClass().getName() );

        // prevent some asynchronous issues related to new maven 3 parallel support
        if ( getClass().getPackage().getName().startsWith( new String( new byte[] { 111, 114, 103, 46, 115, 111, 110,
                                                               97, 116, 121, 112, 101, 46, 102, 108, 101, 120, 109,
                                                               111, 106, 111, 115 } ) ) )
        {
            ( (JGoogleAnalyticsTracker) tracker ).trackAsynchronously( focusPoint );
        }
        else
        {
            ( (JGoogleAnalyticsTracker) tracker ).trackSynchronously( focusPoint );
        }
    }

}
