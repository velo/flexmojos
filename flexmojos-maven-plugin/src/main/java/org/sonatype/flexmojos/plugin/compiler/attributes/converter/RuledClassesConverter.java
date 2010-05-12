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

import org.apache.maven.model.FileSet;
import org.apache.maven.model.PatternSet;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@Component( role = ConfigurationConverter.class, hint = "RuledClasses" )
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

        PatternSet[] classSets = new PatternSet[setNodes.length];
        for ( int i = 0; i < setNodes.length; i++ )
        {
            PatternSet f = new PatternSet();

            PlexusConfiguration includes = setNodes[i].getChild( "includes" );
            for ( PlexusConfiguration include : includes.getChildren(  ) )
            {
                f.addInclude( (String) fromExpression( include, expressionEvaluator, String.class ) );
            }

            PlexusConfiguration excludes = setNodes[i].getChild( "excludes" );
            for ( PlexusConfiguration exclude : excludes.getChildren() )
            {
                f.addExclude( (String) fromExpression( exclude, expressionEvaluator, String.class ) );
            }

            classSets[i] = f;
        }
        rc.setClassSets( classSets );

        return rc;
    }

}
