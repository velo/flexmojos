package org.sonatype.flexmojos.configurator;

import java.util.Map;

import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;

public interface Configurator
{

    void buildConfiguration( ICommandLineConfiguration swf, Map<String, Object> parameters );

    void buildConfiguration( ICompcConfiguration swc, Map<String, Object> parameters );

}
