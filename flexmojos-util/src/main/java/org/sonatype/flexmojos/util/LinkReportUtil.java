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
