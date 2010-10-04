package org.sonatype.flexmojos.coverage;

import apparat.tools.coverage.Coverage.CoverageTool;
import apparat.tools.coverage.CoverageObserver;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.coverage.util.ApparatUtil;
import org.sonatype.flexmojos.util.PathUtil;

import java.io.File;

public abstract class AbstractCoverageReporter
    extends AbstractLogEnabled
    implements CoverageReporter
{

    public void instrument( File swf, File... sourcePaths )
    {
        getLogger().debug( "Instrumenting code to test coverage mode " + System.getProperty( "apparat.threads" ) );
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().info( "Instrumenting: " + PathUtil.path( swf ) + "\n source paths: \n"
                                  + PathUtil.pathString( sourcePaths ) );
        }
        else
        {
            getLogger().info( "Instrumenting: " + PathUtil.path( swf ) );
        }

        CoverageTool c = new CoverageTool();
        c.configure( new CoverageConfigurationImpl( swf, swf, sourcePaths ) );
        c.addObserver( getInstumentationObserver() );

        if ( getLogger().isDebugEnabled() )
        {
            c.addObserver( new CoverageObserver()
            {
                public void instrument( String file, int line )
                {
                    getLogger().debug( "Instrumenting " + ApparatUtil.toClassname( file ) + ":" + line );
                }
            } );
        }
        c.run();

    }

    protected abstract CoverageObserver getInstumentationObserver();

}
