package org.sonatype.flexmojos.plugin.common;

import org.sonatype.flexmojos.plugin.AbstractMavenMojo;

public aspect SkipExecution
{
    pointcut beforeExecute() :  (target(AbstractMavenMojo) )&&
        execution(void execute() ) ;

    void around() : beforeExecute() {
        AbstractMavenMojo mojo = (AbstractMavenMojo) thisJoinPoint.getThis();
        if(mojo.isSkip()) {
            mojo.getLog().warn( "Skipping flexmojos goal execution." );
            return;
        }
        
        proceed();
    }
}
