package org.sonatype.flexmojos.common;

import org.apache.maven.plugin.AbstractMojo;
import org.sonatype.flexmojos.utilities.MavenUtils;

public aspect CommandLineLicenseInfo
{
    pointcut execute() :  target(AbstractMojo) &&
        execution(void execute() ) ;

    before() : execute() {
        AbstractMojo mojo = (AbstractMojo) thisJoinPoint.getThis();
        mojo.getLog().info( "Flexmojos " + MavenUtils.getFlexMojosVersion() );
        mojo.getLog().info( "\t Apache License - Version 2.0 (NO WARRANTY) - See COPYRIGHT file" );
    }
}
