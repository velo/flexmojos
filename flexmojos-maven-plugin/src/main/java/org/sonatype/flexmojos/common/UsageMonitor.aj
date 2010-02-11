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
        
        JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker("Flexmojos",MavenUtils.getFlexMojosVersion(),"UA-3939074-3");
        FocusPoint focusPoint = new FocusPoint(mojo.getClass().getName());
        tracker.trackAsynchronously(focusPoint);
    }

}
