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
package info.flexmojos.generator;

import org.apache.maven.plugin.logging.Log;
import org.granite.generator.GenerationListener;
import org.granite.generator.as3.JavaFileGenerationUnit;

/**
 * Logging <code>GenerationListener</code>.
 * 
 * @author Juraj Burian
 */
public class Gas3Listener
    implements GenerationListener<JavaFileGenerationUnit>
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

    public void error( String message, Exception e )
    {
        log.error( message, e );
    }

    public void generating( JavaFileGenerationUnit unit )
    {
        info( "  Generating: " + unit.getOutput() );
    }

    public void info( String message )
    {
        info( message, null );
    }

    public void info( String message, Exception e )
    {
        log.info( message, e );
    }

    public void warn( String message )
    {
        warn( message, null );
    }

    public void warn( String message, Exception e )
    {
        log.warn( message, e );
    }

}
