/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.generator;

import org.apache.maven.plugin.logging.Log;
import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;

/**
 * Logging <code>GenerationListener</code>.
 * 
 * @author Juraj Burian
 */
public class Gas3Listener
    implements Listener
{

    private final Log log;

    /**
     * @param log
     */
    public Gas3Listener( final Log log )
    {
        this.log = log;
    }

    public void error( String message )
    {
        error( message, null );
    }

    public void error( String message, Throwable e )
    {
        log.error( message, e );
    }

    public void info( String message )
    {
        info( message, null );
    }

    public void info( String message, Throwable e )
    {
        log.info( message, e );
    }

    public void warn( String message )
    {
        warn( message, null );
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
