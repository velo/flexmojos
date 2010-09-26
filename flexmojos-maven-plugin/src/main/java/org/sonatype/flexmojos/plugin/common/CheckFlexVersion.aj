package org.sonatype.flexmojos.plugin.common;

import org.sonatype.flexmojos.plugin.compiler.AbstractFlexCompilerMojo;

public aspect CheckFlexVersion
{
    pointcut beforeExecute() :  (target(AbstractFlexCompilerMojo) )&&
        execution(void execute() ) ;

    void around() : beforeExecute() {
        AbstractFlexCompilerMojo<?, ?> mojo = (AbstractFlexCompilerMojo<?, ?>) thisJoinPoint.getThis();
        mojo.versionCheck();

        proceed();
    }
}
