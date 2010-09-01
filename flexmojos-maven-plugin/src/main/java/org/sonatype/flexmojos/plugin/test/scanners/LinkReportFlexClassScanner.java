package org.sonatype.flexmojos.plugin.test.scanners;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
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

    @SuppressWarnings( "unchecked" )
    protected void removeUnlinkedIncludedFiles( List<String> found, File basedir, File linkReport )
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
