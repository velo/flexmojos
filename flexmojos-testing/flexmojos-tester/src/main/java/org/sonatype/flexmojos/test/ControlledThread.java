package org.sonatype.flexmojos.test;

public interface ControlledThread
{

    void stop();

    ThreadStatus getStatus();

    Throwable getError();

    void lock();

    void unlock();

}
