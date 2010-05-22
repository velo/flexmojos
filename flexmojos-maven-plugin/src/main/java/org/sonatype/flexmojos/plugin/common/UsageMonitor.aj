package org.sonatype.flexmojos.plugin.common;

import org.apache.maven.execution.MavenSession;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;

public aspect UsageMonitor
{

    pointcut execute() : target(AbstractMavenMojo) && execution(void execute() );

    before() : execute() 
    {
        final AbstractMavenMojo mojo = (AbstractMavenMojo) thisJoinPoint.getThis();

        MavenSession s = mojo.getSession();

        JGoogleAnalyticsTracker tracker =
            (JGoogleAnalyticsTracker) s.getSystemProperties().get( "JGoogleAnalyticsTracker" );
        if ( tracker == null )
        {
            byte[] bytes = new byte[] { 85, 65, 45, 51, 57, 51, 57, 48, 55, 52, 45, 51 };
            tracker = new JGoogleAnalyticsTracker( "Flexmojos", MavenUtils.getFlexMojosVersion(), new String( bytes ) );
            s.getSystemProperties().put( "JGoogleAnalyticsTracker", tracker );
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
