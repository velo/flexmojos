package org.sonatype.flexmojos.plugin.common;

import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.compiler.AbstractFlexCompilerMojo;

public aspect QuickMode
{
    private static final String FLEXMOJOS_QUICK_MODE_ACTIVE = "Flexmojos-quick-mode-active";

    pointcut compilerSkipper() :  (target(AbstractFlexCompilerMojo) )&&
        execution(void execute() ) ;

    pointcut genericSkipper() :  (target (AbstractMavenMojo) && !target(AbstractFlexCompilerMojo) )&&
    execution(void execute() ) ;

    void around() : genericSkipper() {
        AbstractMavenMojo mojo = (AbstractMavenMojo) thisJoinPoint.getThis();
        if ( mojo.getPluginContext().containsKey( FLEXMOJOS_QUICK_MODE_ACTIVE ) )
        {
            mojo.getLog().info( "Quick mode kick in, no need to recompile the project." );
            return;
        }

        proceed();
    }

    void around() : compilerSkipper() {
        AbstractFlexCompilerMojo<?, ?> mojo = (AbstractFlexCompilerMojo<?, ?>) thisJoinPoint.getThis();
        if ( !mojo.isCompilationRequired() )
        {
            mojo.getLog().info( "Quick mode kick in, no need to recompile the project." );
            mojo.getPluginContext().put( FLEXMOJOS_QUICK_MODE_ACTIVE, true );
            return;
        }

        proceed();
    }
}
