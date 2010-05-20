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
package org.sonatype.flexmojos.plugin.compiler.attributes.converter;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@Component( role = ConfigurationConverter.class, hint = ModuleConverter.ID )
public class ModuleConverter
    extends AbstractConfigurationConverter
{

    public static final String ID = "Module";

    @SuppressWarnings( "unchecked" )
    public boolean canConvert( Class type )
    {
        return Module.class.equals( type );
    }

    @SuppressWarnings( "unchecked" )
    public Object fromConfiguration( ConverterLookup converterLookup, PlexusConfiguration cfg, Class type,
                                     Class baseType, ClassLoader classLoader, ExpressionEvaluator expressionEvaluator,
                                     ConfigurationListener listener )
        throws ComponentConfigurationException
    {

        String value;
        try
        {
            value = (String) fromExpression( cfg, expressionEvaluator, String.class );
        }
        catch ( ComponentConfigurationException e )
        {
            value = null;
        }

        if ( cfg.getChildCount() == 0 && value == null )
        {
            throw new ComponentConfigurationException( "Invalid configuration it is empty!" );
        }

        Module rc = new Module();

        if ( value != null )
        {
            rc.setSourceFile( value );
        }
        else
        {
            rc.setDestinationPath( (String) fromExpression( cfg.getChild( "destinationPath" ), expressionEvaluator,
                                                            String.class ) );
            rc.setFinalName( (String) fromExpression( cfg.getChild( "finalName" ), expressionEvaluator, String.class ) );
            rc.setOptimize( Boolean.parseBoolean( (String) fromExpression( cfg.getChild( "optimize" ),
                                                                           expressionEvaluator, String.class ) ) );
            rc.setSourceFile( (String) fromExpression( cfg.getChild( "sourceFile" ), expressionEvaluator, String.class ) );
        }

        return rc;
    }

}
