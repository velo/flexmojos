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
package org.sonatype.flexmojos.plugin.test.scanners;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;

public abstract class AbstractFlexClassScanner
    extends AbstractLogEnabled
    implements FlexClassScanner
{

    protected List<String> classes;

    public AbstractFlexClassScanner()
    {
        super();
    }

    protected List<String> scan( File dir, String[] exclusions, Map<String, Object> context )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( dir );
        scanner.setIncludes( new String[] { "**/*.as", "**/*.mxml" } );
        scanner.setExcludes( exclusions );
        scanner.addDefaultExcludes();
        scanner.scan();

        return new ArrayList<String>( Arrays.asList( scanner.getIncludedFiles() ) );
    }

    public List<String> getAs3Classes()
    {
        return classes;
    }

}