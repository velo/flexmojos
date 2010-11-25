package org.sonatype.flexmojos.plugin.compiler.attributes.converter;

import org.apache.maven.model.FileSet;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@SuppressWarnings( "all" )
public class SimplifiablePatternConverter
    extends AbstractConfigurationConverter
{

    public static final String ID = "SimplifiablePattern";

    public boolean canConvert( Class type )
    {
        return SimplifiablePattern.class.equals( type );
    }

    public Object fromConfiguration( ConverterLookup converterLookup, PlexusConfiguration cfg, Class type,
                                     Class baseType, ClassLoader classLoader, ExpressionEvaluator expressionEvaluator,
                                     ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        SimplifiablePattern rc = new SimplifiablePattern();
        if ( cfg.getChildCount() == 0 )
        {
            throw new ComponentConfigurationException( "Invalid configuration it is empty!" );
        }

        PlexusConfiguration[] nodes = cfg.getChildren();
        for ( PlexusConfiguration node : nodes )
        {
            String name = node.getName();
            if ( !( name.equals( "include" ) || name.equals( "scan" ) ) )
            {
                throw new ComponentConfigurationException( "Invalid configuration: '" + name
                    + "'. Valid values are 'include' and 'scan'" );
            }
        }

        PlexusConfiguration[] directIncludeNodes = cfg.getChildren( "include" );
        PlexusConfiguration[] patternNodes = cfg.getChildren( "scan" );

        if ( directIncludeNodes.length == 0 && patternNodes.length == 0 )
        {
            return rc;
        }

        for ( PlexusConfiguration includeNode : directIncludeNodes )
        {
            rc.addInclude( (String) fromExpression( includeNode, expressionEvaluator, String.class ) );
        }

        for ( PlexusConfiguration patternNode : patternNodes )
        {
            FileSet f = new FileSet();

            f.setDirectory( (String) fromExpression( patternNode.getChild( "directory" ), expressionEvaluator,
                                                     String.class ) );
            PlexusConfiguration includes = patternNode.getChild( "includes" );
            for ( PlexusConfiguration include : includes.getChildren() )
            {
                f.addInclude( (String) fromExpression( include, expressionEvaluator, String.class ) );
            }

            PlexusConfiguration excludes = patternNode.getChild( "excludes" );
            for ( PlexusConfiguration exclude : excludes.getChildren() )
            {
                f.addExclude( (String) fromExpression( exclude, expressionEvaluator, String.class ) );
            }

            rc.addPattern( f );
        }

        return rc;
    }

}
