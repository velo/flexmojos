package org.sonatype.flexmojos.plugin.test.scanners;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface FlexClassScanner
{

    void scan( File[] directories, String[] exclusions, Map<String, Object> context );

    List<String> getAs3Classes();

    List<String> getAs3Snippets();

}
