package org.sonatype.flexmojos.idesupport;

import org.sonatype.flexmojos.compiler.*;
import org.apache.maven.plugin.eclipse.EclipsePlugin;

public aspect SwcSwfInherit
{
    declare parents : AbstractIdeMojo extends EclipsePlugin;
}
