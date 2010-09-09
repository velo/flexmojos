package org.sonatype.flexmojos.plugin.test.scanners;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = FlexClassScanner.class, hint = "disabled" )
public class DisableFlexClassScanner
    implements FlexClassScanner
{

    public List<String> getAs3Classes()
    {
        return Collections.emptyList();
    }

    public List<String> getAs3Snippets()
    {
        return Collections.emptyList();
    }

    public void scan( File[] directories, String[] exclusions, Map<String, Object> context )
    {
    }

}
