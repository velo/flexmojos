package bcel.helper;

import org.sonatype.flexmojos.utilities.FlexmojosCompilerAPIHelper;

import flash.localization.LocalizationManager;
import flex2.compiler.Logger;
import flex2.compiler.common.PathResolver;

public class Tampered
{
    private static ThreadLocal<Logger> logger = new ThreadLocal<Logger>();
    private static ThreadLocal<PathResolver> resolver = new ThreadLocal<PathResolver>();
    private static ThreadLocal<LocalizationManager> localization = new ThreadLocal<LocalizationManager>();

    public static void setPathResolver(PathResolver r)
    {
        r = FlexmojosCompilerAPIHelper.fixPathResolver(r);
        resolver.set(r);
    }

    public static void setLogger(Logger logger)
    {
        logger = FlexmojosCompilerAPIHelper.fixLogger(logger);
        Tampered.logger.set(logger);
        if (logger != null)
        {
            logger.setLocalizationManager( getLocalizationManager() );
        }
    }

    public static LocalizationManager getLocalizationManager()
    {
        return localization.get();
    }
}
