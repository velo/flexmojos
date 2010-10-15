package org.sonatype.flexmojos.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.hamcrest.MatcherAssert;
import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class Flexmojos126Test
    extends AbstractIssueTest
{

    @SuppressWarnings( { "unchecked" } )
    @Test
    public void generatorClasspathAccess()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-126" );
        File linkReportXml =
            new File(
                      v.getBasedir(),
                      "FlexMojos-Jira-126-FlexSide/target/FlexMojos-Jira-126-FlexSide-1.0-SNAPSHOT-link-report.xml" );

        MatcherAssert.assertThat( linkReportXml, FileMatcher.exists() );

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read( linkReportXml );

        List<String> linkedFiles = new ArrayList<String>();
        List<Attribute> list = document.selectNodes( "//script/@name" );
        for ( Attribute attribute : list )
        {
            linkedFiles.add( attribute.getValue() );
        }

        assertThat( linkedFiles, hasItems( endsWith( "AdminMessage.as" ), endsWith( "AdminMessageBase.as" ) ) );
    }

}
