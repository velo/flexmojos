package org.sonatype.flexmojos.plugin.utilities;

import java.io.File;

public class ConfigurationResolver
{
    public static File[] resolveConfiguration( File[] configs, File referenceConfig, File configDirectory )
    {
        if ( referenceConfig == null && configs != null )
        {
            return configs;
        }

        File configFile;
        if ( referenceConfig != null )
        {
            configFile = referenceConfig;
        }
        else
        {
            File cfg = new File( configDirectory, "config.xml" );
            File flexCfg = new File( configDirectory, "flex-config.xml" );
            File airCfg = new File( configDirectory, "air-config.xml" );
            if ( cfg.exists() )
            {
                configFile = cfg;
            }
            else if ( flexCfg.exists() )
            {
                configFile = flexCfg;
            }
            else if ( airCfg.exists() )
            {
                configFile = airCfg;
            }
            else
            {
                return new File[0];
            }
        }
        return new File[] { configFile };
    }
}
