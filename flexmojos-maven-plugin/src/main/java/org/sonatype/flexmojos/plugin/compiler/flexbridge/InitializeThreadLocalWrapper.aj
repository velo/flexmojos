package org.sonatype.flexmojos.plugin.compiler.flexbridge;

import org.sonatype.flexmojos.compiler.util.ThreadLocalToolkitHelper;
import org.sonatype.flexmojos.plugin.compiler.AbstractFlexCompilerMojo;

public aspect InitializeThreadLocalWrapper
{
    pointcut execute() :   target(AbstractFlexCompilerMojo)  &&
        execution(void execute() ) ;

    @SuppressWarnings( "unchecked" )
    before() : execute() {
        AbstractFlexCompilerMojo mojo = (AbstractFlexCompilerMojo) thisJoinPoint.getThis();
        ThreadLocalToolkitHelper.setMavenLogger( mojo.getMavenLogger() );
        ThreadLocalToolkitHelper.setMavenResolver( mojo.getMavenPathResolver() );
    }
}
