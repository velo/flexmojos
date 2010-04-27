/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
