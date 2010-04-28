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
package org.sonatype.flexmojos.generator.granitedsv1d1d0;

import org.codehaus.plexus.logging.Logger;
import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;

/**
 * Logging <code>GenerationListener</code>.
 * 
 * @author Juraj Burian
 */
final class Gas3Listener
    implements Listener
{

    private final Logger log;

    /**
     * @param log
     */
    public Gas3Listener( final Logger log )
    {
        this.log = log;
    }

    public void error( String message )
    {
        this.log.error( message );
    }

    public void error( String message, Throwable e )
    {
        log.error( message, e );
    }

    public void info( String message )
    {
        this.log.info( message );
    }

    public void info( String message, Throwable e )
    {
        log.info( message, e );
    }

    public void warn( String message )
    {
        this.log.warn( message );
    }

    public void warn( String message, Throwable e )
    {
        log.warn( message, e );
    }

    public void generating( Input<?> input, Output<?> output )
    {
        info( "  Generating: " + output.getDescription() );
    }

    public void skipping( Input<?> input, Output<?> output )
    {
        info( "  Skipping: " + output.getDescription() );
    }

}
