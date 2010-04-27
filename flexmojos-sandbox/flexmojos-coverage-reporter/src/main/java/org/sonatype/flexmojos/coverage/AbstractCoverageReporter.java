package org.sonatype.flexmojos.coverage;

import java.io.File;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.coverage.util.ApparatUtil;
import org.sonatype.flexmojos.util.PathUtil;

import apparat.tools.ApparatConfiguration;
import apparat.tools.coverage.CoverageObserver;
import apparat.tools.coverage.Coverage.CoverageTool;

public abstract class AbstractCoverageReporter
    extends AbstractLogEnabled
    implements CoverageReporter
{

    public void instrument( File swf, File... sourcePaths )
    {

        getLogger().debug( "Instrumenting code to test coverage mode " + System.getProperty( "apparat.threads" ) );

        ApparatConfiguration cfg = new ApparatConfiguration();
        cfg.update( "-i", PathUtil.getCanonicalPath( swf ) );
        cfg.update( "-s", PathUtil.getCanonicalPathString( sourcePaths ) );

        CoverageTool c = new CoverageTool();
        c.configure( cfg );
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
