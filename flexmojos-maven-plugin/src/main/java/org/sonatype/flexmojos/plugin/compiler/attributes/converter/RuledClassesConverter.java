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
