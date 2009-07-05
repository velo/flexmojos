/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
