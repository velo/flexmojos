package org.sonatype.flexmojos.configurator.sample;

import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.configurator.Configurator;

public class SampleConfigurator
    implements Configurator
{

    public void buildConfiguration( ICommandLineConfiguration swf )
    {
        System.out.println( "Running configurator for a SWF project." );
        System.out.println( swf.getOutput() );
    }

    public void buildConfiguration( ICompcConfiguration swc )
    {
        System.out.println( "Running configurator for a SWC project." );
        System.out.println( swc.getOutput() );
    }

}
