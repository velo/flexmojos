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
/**
 * 
 */
package org.sonatype.flexmojos.compiler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

public class MapLogger
    extends AbstractLogger
{

    private final Map<String, Throwable> logs;

    public MapLogger()
    {
        super( Logger.LEVEL_DEBUG, "TestLog" );
        this.logs = new LinkedHashMap<String, Throwable>();
    }

    public void debug( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

    public void error( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

    public void fatalError( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

    public Logger getChildLogger( String name )
    {
        return this;
    }

    public Map<String, Throwable> getLogs()
    {
        return logs;
    }

    public void info( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

    public void warn( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

}
