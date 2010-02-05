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
package org.sonatype.flexmojos.common.converter;

import org.apache.maven.model.FileSet;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@Component(role=ConfigurationConverter.class, hint="RuledClasses")
public class RuledClassesConverter
    extends AbstractConfigurationConverter
{

    @SuppressWarnings( "unchecked" )
    public boolean canConvert( Class type )
    {
        return RuledClasses.class.equals( type );
    }

    @SuppressWarnings( "unchecked" )
    public Object fromConfiguration( ConverterLookup converterLookup, PlexusConfiguration cfg, Class type,
                                     Class baseType, ClassLoader classLoader, ExpressionEvaluator expressionEvaluator,
                                     ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object retValue = fromExpression( cfg, expressionEvaluator, type );
        if ( retValue != null )
        {
            return retValue;
        }

        Class implementation = getClassForImplementationHint( type, cfg, classLoader );

        retValue = instantiateObject( implementation );

        RuledClasses rc = new RuledClasses();
        if ( cfg.getChildCount() == 0 )
        {
            throw new ComponentConfigurationException( "Invalid configuration it is empty!" );
        }

        PlexusConfiguration[] nodes = cfg.getChildren();
        for ( PlexusConfiguration node : nodes )
        {
            String name = node.getName();
            if ( !( name.equals( "class" ) || name.equals( "classSet" ) ) )
            {
                throw new ComponentConfigurationException( "Invalid configuration: '" + name
                    + "'. Valid values are 'class' and 'classSet'" );
            }
        }

        PlexusConfiguration[] classNodes = cfg.getChildren( "class" );
        PlexusConfiguration[] setNodes = cfg.getChildren( "classSet" );

        if ( classNodes.length == 0 && setNodes.length == 0 )
        {
            return rc;
        }

        String[] classes = new String[classNodes.length];
        for ( int i = 0; i < classNodes.length; i++ )
        {
            classes[i] = (String) fromExpression( classNodes[i], expressionEvaluator, String.class );
        }
        rc.setClasses( classes );

        FileSet[] classSets = new FileSet[setNodes.length];
        for ( int i = 0; i < setNodes.length; i++ )
        {
            classSets[i] = (FileSet) fromExpression( setNodes[i], expressionEvaluator, FileSet.class );
        }
        rc.setClassSets( classSets );

        return rc;
    }

}
