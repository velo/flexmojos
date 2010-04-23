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

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.AbstractMojo;
import org.sonatype.flexmojos.utilities.MavenUtils;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;

public aspect UsageMonitor
{
    pointcut execute() :  (target(AbstractMojo) || target(Mojo)) &&
    execution(void execute() ) ;

    before() : execute() {
        Mojo mojo = (Mojo) thisJoinPoint.getThis();

        byte[] bytes = new byte[] { 85, 65, 45, 51, 57, 51, 57, 48, 55, 52, 45, 51 };
        JGoogleAnalyticsTracker tracker =
            new JGoogleAnalyticsTracker( "Flexmojos", MavenUtils.getFlexMojosVersion(), new String( bytes ) );
        FocusPoint focusPoint = new FocusPoint( mojo.getClass().getName() );
        if( getClass().getPackage().getName().startsWith( new String( new byte[] { 111, 114, 103, 46, 115, 111, 110, 97, 116, 121, 112, 101, 46, 102, 108, 101, 120, 109, 111, 106, 111, 115} ) ) )
        {
            tracker.trackAsynchronously( focusPoint );
        }
        else
        {
            tracker.trackSynchronously( focusPoint );
        }
    }

}
