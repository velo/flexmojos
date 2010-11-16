package org.sonatype.flexmojos.plugin.compiler.attributes.converter;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class ModuleConverter
    extends AbstractConfigurationConverter
{

    public static final String ID = "Module";

    @SuppressWarnings( "all" )
    public boolean canConvert( Class type )
    {
        return Module.class.equals( type );
    }

    @SuppressWarnings( "all" )
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
