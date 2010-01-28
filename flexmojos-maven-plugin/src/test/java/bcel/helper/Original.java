package bcel.helper;

import flash.localization.LocalizationManager;
import flex2.compiler.Logger;
import flex2.compiler.common.PathResolver;

public class Original
{
    private static ThreadLocal<Logger> logger = new ThreadLocal<Logger>();
    private static ThreadLocal<PathResolver> resolver = new ThreadLocal<PathResolver>();
    private static ThreadLocal<LocalizationManager> localization = new ThreadLocal<LocalizationManager>();

    public static void setPathResolver(PathResolver r)
    {
        resolver.set(r);
    }

    public static void setLogger(Logger logger)
    {
        Original.logger.set(logger);
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
