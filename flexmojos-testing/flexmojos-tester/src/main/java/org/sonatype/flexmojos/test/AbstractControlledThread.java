/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.logging.AbstractLogEnabled;

public abstract class AbstractControlledThread
    extends AbstractLogEnabled
    implements ControlledThread, Runnable
{

    protected ThreadStatus status;

    protected Throwable error;

    private ReentrantLock lock;

    protected void launch()
    {
        Thread t = new Thread( this );
        t.setUncaughtExceptionHandler( new UncaughtExceptionHandler()
        {
            public void uncaughtException( Thread t, Throwable e )
            {
                if ( !ThreadStatus.ERROR.equals( status ) )
                {
                    status = ThreadStatus.ERROR;
                    error = e;
                }
                getLogger().debug( "[MOJO] Error running: " + getClass(), e );
            }
        } );

        t.setDaemon( true );
        t.start();
        Thread.yield();
    }

    public final ThreadStatus getStatus()
    {
        return status;
    }

    public final Throwable getError()
    {
        return this.error;
    }

    public void lock()
    {
        if ( lock != null )
        {
            return;
        }

        lock = new ReentrantLock();
        lock.lock();
    }

    public void unlock()
    {
        if ( lock == null )
        {
            return;
        }

        lock.unlock();
    }

    protected void reset()
    {
        status = null;
        error = null;
    }

}
