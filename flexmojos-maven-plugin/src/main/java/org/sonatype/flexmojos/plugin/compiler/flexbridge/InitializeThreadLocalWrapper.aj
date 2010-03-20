package org.sonatype.flexmojos.plugin.compiler.flexbridge;

import org.sonatype.flexmojos.compiler.util.ThreadLocalToolkitHelper;
import org.sonatype.flexmojos.plugin.compiler.AbstractMavenFlexCompilerConfiguration;

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
