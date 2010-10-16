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
package org.sonatype.flexmojos.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class LinkReportUtil
{

    @SuppressWarnings( "unchecked" )
    public static List<String> getLinkedFiles( File linkReport )
    {
        List<String> linkedFiles = new ArrayList<String>();
        SAXReader saxReader = new SAXReader();
        Document document;
        try
        {
            document = saxReader.read( linkReport );
        }
        catch ( DocumentException e )
        {
            throw new IllegalStateException( "Error removing unlinked includes using link report '"
                + linkReport.getAbsolutePath() + "'.", e );
        }

        List<Attribute> list = document.selectNodes( "//script/@name" );
        for ( Attribute attribute : list )
        {
            linkedFiles.add( attribute.getValue() );
        }
        return linkedFiles;
    }
}
