package org.sonatype.flexmojos.configurator.sample;

import java.io.File;
import java.util.Map;

import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.configurator.Configurator;
import org.sonatype.flexmojos.configurator.ConfiguratorException;

public class SampleConfigurator
    implements Configurator
{

    public void buildConfiguration( ICommandLineConfiguration swf, File sourceFile, Map<String, Object> parameters )
        throws ConfiguratorException
    {
        System.out.println( "Running configurator for a SWF project." );
        System.out.println( swf.getOutput() );
    }

    public void buildConfiguration( ICompcConfiguration swc, Map<String, Object> parameters )
    {
        System.out.println( "Running configurator for a SWC project." );
        System.out.println( swc.getOutput() );
    }

}
