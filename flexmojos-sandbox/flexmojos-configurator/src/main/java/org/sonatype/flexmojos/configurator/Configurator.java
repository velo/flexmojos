package org.sonatype.flexmojos.configurator;

import java.io.File;
import java.util.Map;

import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;

public interface Configurator
{

    void buildConfiguration( ICommandLineConfiguration swf, File sourceFile, Map<String, Object> parameters )
        throws ConfiguratorException;

    void buildConfiguration( ICompcConfiguration swc, Map<String, Object> parameters )
        throws ConfiguratorException;

}
