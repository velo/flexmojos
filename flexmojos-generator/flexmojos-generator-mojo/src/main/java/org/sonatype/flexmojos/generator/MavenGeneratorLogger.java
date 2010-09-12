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
package org.sonatype.flexmojos.generator;

import org.apache.maven.plugin.logging.Log;

public class MavenGeneratorLogger
    implements GeneratorLogger
{

    private Log log;

    public MavenGeneratorLogger( Log log )
    {
        this.log = log;
    }

    public void debug( String message )
    {
        this.log.debug( message );
    }

    public void debug( String message, Throwable throwable )
    {
        this.log.debug( message, throwable );
    }

    public boolean isDebugEnabled()
    {
        return this.isDebugEnabled();
    }

    public void info( String message )
    {
        this.log.info( message );
    }

    public void info( String message, Throwable throwable )
    {
        this.log.info( message, throwable );
    }

    public boolean isInfoEnabled()
    {
        return this.log.isInfoEnabled();
    }

    public void warn( String message )
    {
        this.log.warn( message );
    }

    public void warn( String message, Throwable throwable )
    {
        this.log.warn( message, throwable );
    }

    public boolean isWarnEnabled()
    {
        return this.log.isWarnEnabled();
    }

    public void error( String message )
    {
        this.log.error( message );
    }

    public void error( String message, Throwable throwable )
    {
        this.log.error( message, throwable );
    }

}
