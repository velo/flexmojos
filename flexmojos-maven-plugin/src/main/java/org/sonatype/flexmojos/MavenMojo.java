package org.sonatype.flexmojos;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;

public interface MavenMojo
    extends Mojo
{
    MavenSession getSession();
}
