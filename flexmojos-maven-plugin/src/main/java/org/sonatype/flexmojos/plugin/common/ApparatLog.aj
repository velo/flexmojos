package org.sonatype.flexmojos.plugin.common;

import org.sonatype.flexmojos.plugin.AbstractMavenMojo;

import apparat.embedding.maven.MavenLogAdapter;
import apparat.log.Log;

public aspect ApparatLog
{
    pointcut execute() :  target(AbstractMavenMojo) &&
        execution(void execute() ) ;

    private MavenLogAdapter logAdapter;

    before() : execute() {
        AbstractMavenMojo mojo = (AbstractMavenMojo) thisJoinPoint.getThis();
        this.logAdapter = new MavenLogAdapter( mojo.getLog() );
        Log.setLevel( logAdapter.getLevel() );
        Log.addOutput( logAdapter );
    }

    after() : execute() {
        Log.removeOutput( logAdapter );
    }
}
