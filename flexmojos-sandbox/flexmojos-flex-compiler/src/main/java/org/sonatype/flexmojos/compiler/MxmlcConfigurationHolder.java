package org.sonatype.flexmojos.compiler;

import java.io.File;

public final class MxmlcConfigurationHolder
{
    ICommandLineConfiguration configuration;

    File sourceFile;

    public MxmlcConfigurationHolder( ICommandLineConfiguration configuration, File sourceFile )
    {
        super();
        this.configuration = configuration;
        this.sourceFile = sourceFile;
    }

    public ICommandLineConfiguration getConfiguration()
    {
        return configuration;
    }

    public File getSourceFile()
    {
        return sourceFile;
    }

    public void setConfiguration( ICommandLineConfiguration configuration )
    {
        this.configuration = configuration;
    }

    public void setSourceFile( File sourceFile )
    {
        this.sourceFile = sourceFile;
    }
}
