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

import static org.sonatype.flexmojos.util.LinkReportUtil.getLinkedFiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.flexmojos.plugin.common.FlexClassifier;

@Component( role = FlexClassScanner.class, hint = "link-report" )
public class LinkReportFlexClassScanner
    extends AbstractFlexClassScanner
{

    public void scan( File[] directories, String[] exclusions, Map<String, Object> context )
    {
        classes = new ArrayList<String>();

        for ( File dir : directories )
        {
            List<String> found = scan( dir, exclusions, context );
            removeUnlinkedIncludedFiles( found, dir, (File) context.get( FlexClassifier.LINK_REPORT ) );
            classes.addAll( found );
        }
    }

    protected void removeUnlinkedIncludedFiles( List<String> found, File basedir, File linkReport )
    {
        List<String> linkedFiles = getLinkedFiles( linkReport );

        String baseDir = basedir.getAbsolutePath().concat( File.separator );
        for ( Iterator<String> iterator = found.iterator(); iterator.hasNext(); )
        {
            String includedFile = iterator.next();
            if ( !linkedFiles.contains( baseDir.concat( includedFile ) ) )
            {
                iterator.remove();
            }
        }
    }

    public List<String> getAs3Snippets()
    {
        return Collections.emptyList();
    }
}
