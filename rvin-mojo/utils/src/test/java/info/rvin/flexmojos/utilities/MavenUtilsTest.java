package info.rvin.flexmojos.utilities;

import java.io.File;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringInputStream;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

public class MavenUtilsTest
{

    private String XML = "<flex-config>" + //
        "<compiler>" + //
        "<namespaces>" + //
        "<namespace>" + //
        "<uri>http://www.adobe.com/2006/mxml</uri>" + //
        "<manifest>mxml-manifest.xml</manifest>" + //
        "</namespace>" + //
        "</namespaces>" + //
        "</compiler>" + //
        "</flex-config>";//

    @Test
    public void testReadNamespaces()
        throws Exception
    {
        SAXBuilder parser = new SAXBuilder();
        Document document = parser.build( new StringInputStream( XML ) );

        Map<String, File> namespaces = MavenUtils.readNamespaces( new File( "./" ), document );
        Assert.assertEquals( 1, namespaces.size() );
    }

}
