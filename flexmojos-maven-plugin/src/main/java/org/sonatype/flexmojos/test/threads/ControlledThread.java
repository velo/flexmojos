package org.sonatype.flexmojos.test.threads;

public interface ControlledThread
    extends Runnable
{

    void stop();

    ThreadStatus getStatus();

    Error getError();

    void setStatus( ThreadStatus status );

    void setError( Error error );

}
