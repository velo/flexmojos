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

//        try
//        {
//            Class<?> tlt = Class.forName( "flex2.compiler.util.ThreadLocalToolkit" );
//            tlt.getDeclaredField( "assertor" );
//        }
//        catch ( Exception e )
//        {
//            throw new IllegalStateException( "Flexmojos didn't loaded the ThreadLocalToolkit properly.", e );
//        }

        ThreadLocalToolkitHelper.setMavenLogger( mojo.getMavenLogger() );
        ThreadLocalToolkitHelper.setMavenResolver( mojo.getMavenPathResolver() );
    }

}
