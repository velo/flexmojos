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
