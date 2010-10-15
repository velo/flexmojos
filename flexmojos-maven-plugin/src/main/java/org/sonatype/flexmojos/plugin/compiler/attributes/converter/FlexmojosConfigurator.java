package org.sonatype.flexmojos.plugin.compiler.attributes.converter;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@Component( role = ComponentConfigurator.class, hint = "flexmojos" )
public class FlexmojosConfigurator
    extends BasicComponentConfigurator
    implements ComponentConfigurator
{

    public void configureComponent( Object component, PlexusConfiguration configuration,
                                    ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm,
                                    ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        converterLookup.registerConverter( new HexIntConverter() );
        converterLookup.registerConverter( new SimplifiablePatternConverter() );
        converterLookup.registerConverter( new ModuleConverter() );

        super.configureComponent( component, configuration, expressionEvaluator, containerRealm, listener );
    }
}
