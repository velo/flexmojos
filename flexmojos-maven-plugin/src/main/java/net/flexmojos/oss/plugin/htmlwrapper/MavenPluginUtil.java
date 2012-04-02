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
package net.flexmojos.oss.plugin.htmlwrapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Assorted utility methods used by externalizable HtmlWrapperMojo patch. Feel free to move this to a more appropriate
 * package or switch over to equivalent alternative sonatype/plexus methods I overlooked.
 * 
 * @author David Rom (david.s.rom@gmail.com)
 */
public abstract class MavenPluginUtil
{

    /**
     * Extracts a copy of the plugin's configuration as a map. This copy can be safely edited without affecting the
     * plugin itself.
     * 
     * @param plugin
     * @return
     */
    public static Map<String, String> extractParameters( Plugin plugin )
        throws MojoExecutionException
    {
        // Does source pom's flexmojos plugin contain configuration?
        Object config = plugin.getConfiguration();
        if ( config != null )
        {
            if ( !( config instanceof Xpp3Dom ) )
            {
                throw new MojoExecutionException( "Plugin config is of unknown type:  " + config.getClass() );
            }

            Xpp3Dom params = (Xpp3Dom) config;
            Xpp3Dom paramModel = params.getChild( "parameters" );

            // Is there a parameters section configuration?
            if ( paramModel != null )
            {
                return extractParameters( paramModel );
            }
        }

        return null;
    }

    /**
     * Extracts a copy of the Xpp3Dom object as a java.util.Map. This copy can be safely edited without affecting the
     * Xpp3Dom object itself.
     * 
     * @param plugin
     * @return
     */
    public static Map<String, String> extractParameters( Xpp3Dom source )
    {
        HashMap<String, String> retVal = new HashMap<String, String>();
        Xpp3Dom[] children = source.getChildren();

        for ( int i = 0; i < children.length; i++ )
        {
            Xpp3Dom child = children[i];
            retVal.put( child.getName(), child.getValue() );
        }

        return retVal;
    }

    /**
     * Gets the plugin's actual configuration wrapped in a facade with a java.util.Map<String, String> interface. Any
     * edits to the Xpp3DomMap will affect the provided plugin.
     * 
     * @param plugin
     * @return
     */
    public static Xpp3DomMap getParameters( Plugin plugin )
    {
        Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
        if ( config == null )
        {
            config = new Xpp3Dom( "configuration" );
            plugin.setConfiguration( config );
        }

        return new Xpp3DomMap( config );
    }
}
