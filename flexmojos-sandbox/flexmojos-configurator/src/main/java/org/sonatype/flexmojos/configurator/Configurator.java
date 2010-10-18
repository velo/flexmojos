package org.sonatype.flexmojos.configurator;

import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;

public interface Configurator
{

    void buildConfiguration( ICommandLineConfiguration swf );

    void buildConfiguration( ICompcConfiguration swc );

}
