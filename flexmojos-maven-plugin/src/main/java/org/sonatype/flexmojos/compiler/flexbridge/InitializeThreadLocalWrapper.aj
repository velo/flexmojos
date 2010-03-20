package org.sonatype.flexmojos.compiler.flexbridge;

import org.sonatype.flexmojos.compiler.AbstractMavenFlexCompilerConfiguration;
import org.sonatype.flexmojos.compiler.util.ThreadLocalToolkitHelper;

public aspect InitializeThreadLocalWrapper
{
    pointcut execute() :   target(AbstractMavenFlexCompilerConfiguration)  &&
        execution(void execute() ) ;

    @SuppressWarnings( "unchecked" )
    before() : execute() {
        AbstractMavenFlexCompilerConfiguration mojo = (AbstractMavenFlexCompilerConfiguration) thisJoinPoint.getThis();
        ThreadLocalToolkitHelper.setMavenLogger( mojo.getMavenLogger() );
        ThreadLocalToolkitHelper.setMavenResolver( mojo.getMavenPathResolver() );
    }
}
