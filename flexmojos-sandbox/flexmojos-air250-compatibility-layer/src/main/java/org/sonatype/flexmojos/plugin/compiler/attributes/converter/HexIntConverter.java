package org.sonatype.flexmojos.plugin.compiler.attributes.converter;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.basic.IntConverter;

public class HexIntConverter
    extends IntConverter
    implements ConfigurationConverter
{
    public Object fromString( String str )
        throws ComponentConfigurationException
    {
        try
        {
            return Integer.decode( str );
        }
        catch ( NumberFormatException e )
        {
            throw new ComponentConfigurationException( "Not a number: '" + str + "'", e );
        }
    }
}
