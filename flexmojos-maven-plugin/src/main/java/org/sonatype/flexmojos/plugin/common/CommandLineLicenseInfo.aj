package org.sonatype.flexmojos.plugin.common;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;

public aspect CommandLineLicenseInfo
{
    pointcut execute() :  (target(AbstractMojo) || target(Mojo) )&&
        execution(void execute() ) ;

    before() : execute() {
        Mojo mojo = (Mojo) thisJoinPoint.getThis();
        mojo.getLog().info( "Flexmojos " + MavenUtils.getFlexMojosVersion() );
        mojo.getLog().info( "\t Apache License - Version 2.0 (NO WARRANTY) - See COPYRIGHT file" );
    }
}
