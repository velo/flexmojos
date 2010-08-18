package org.sonatype.flexmojos.plugin.common.flexbridge;

import org.sonatype.flexmojos.compiler.util.ThreadLocalToolkitHelper;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;

public aspect InitializeThreadLocalWrapper
{
    pointcut execute() :   target(AbstractMavenMojo)  &&
        execution(void execute() ) ;

    @SuppressWarnings( "unchecked" )
    before() : execute() {
        AbstractMavenMojo mojo = (AbstractMavenMojo) thisJoinPoint.getThis();
        ThreadLocalToolkitHelper.setMavenLogger( mojo.getMavenLogger() );
        ThreadLocalToolkitHelper.setMavenResolver( mojo.getMavenPathResolver() );
    }
    
}
