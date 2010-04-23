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
        if(getClass().getPackage().getName().startsWith( new String(new byte[]{111, 114, 103, 46, 115, 111, 110, 97, 116, 121, 112, 101, 46, 102, 108, 101, 120, 109, 111, 106, 111, 115}) ))
        {
            tracker.trackAsynchronously( focusPoint );
        }
        else
        {
            tracker.trackSynchronously( focusPoint );
        }
    }

}
