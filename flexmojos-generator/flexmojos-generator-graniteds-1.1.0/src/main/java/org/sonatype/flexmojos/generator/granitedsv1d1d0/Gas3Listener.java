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
