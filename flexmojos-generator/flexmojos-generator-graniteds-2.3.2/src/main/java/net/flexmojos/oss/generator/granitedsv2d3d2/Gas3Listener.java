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
package net.flexmojos.oss.generator.granitedsv2d3d2;

import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;
import net.flexmojos.oss.generator.GeneratorLogger;

/**
 * Logging <code>GenerationListener</code>.
 * 
 * @author velo.br@gmail.com
 */
final class Gas3Listener
    implements Listener
{

    private GeneratorLogger logger;

    public Gas3Listener( GeneratorLogger generatorLogger )
    {
        this.logger = generatorLogger;
    }

    public void error( String message )
    {
        this.logger.error( message );
    }

    public void error( String message, Throwable e )
    {
        this.logger.error( message, e );
    }

    public void info( String message )
    {
        this.logger.info( message );
    }

    public void info( String message, Throwable e )
    {
        this.logger.info( message, e );
    }

    public void debug( String message )
    {
        this.logger.debug( message );
    }

    public void debug( String message, Throwable e )
    {
        this.logger.debug( message, e );
    }

    public void warn( String message )
    {
        this.logger.warn( message );
    }

    public void warn( String message, Throwable e )
    {
        this.logger.warn( message, e );
    }

    public void generating( Input<?> input, Output<?> output )
    {
        info( "  Generating: " + output.getDescription() );
    }

    public void generating( String file, String message )
    {
        info( "  Generating: " + file + " - " + message );
    }

    public void skipping( Input<?> input, Output<?> output )
    {
        info( "  Skipping: " + output.getDescription() + " - " + output.getMessage() );
    }

    public void skipping( String file, String message )
    {
        info( "  Skipping: " + file + " - " + message );
    }

    @Override
    public void removing(Input<?> input, Output<?> output) {
        info( "  Removing: " + output.getDescription() );
    }

    @Override
    public void removing(String file, String message) {
        info( "  Removing: " + file + " - " + message );
    }

}
