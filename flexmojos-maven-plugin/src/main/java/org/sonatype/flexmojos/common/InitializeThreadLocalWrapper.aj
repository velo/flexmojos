package org.sonatype.flexmojos.common;

import org.sonatype.flexmojos.compiler.util.ThreadLocalToolkitHelper;
import org.sonatype.flexmojos.flexbridge.MavenLogger;
import org.sonatype.flexmojos.flexbridge.MavenPathResolver;

import flex2.tools.oem.internal.OEMLogAdapter;

public aspect InitializeThreadLocalWrapper
{
    pointcut execute() :   target(AbstractMavenFlexCompilerConfiguration)  &&
        execution(void execute() ) ;

    before() : execute() {
        AbstractMavenFlexCompilerConfiguration mojo = (AbstractMavenFlexCompilerConfiguration) thisJoinPoint.getThis();
        ThreadLocalToolkitHelper.setMavenLogger( new OEMLogAdapter( new MavenLogger( mojo.getLog() ) ) );
        ThreadLocalToolkitHelper.setMavenResolver( new MavenPathResolver( mojo.resources ) );
    }
}
