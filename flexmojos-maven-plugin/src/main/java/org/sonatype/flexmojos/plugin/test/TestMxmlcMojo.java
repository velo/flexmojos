package org.sonatype.flexmojos.plugin.test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.anyOf;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.scope;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.INTERNAL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.TEST;

import java.io.File;

import org.sonatype.flexmojos.plugin.compiler.MxmlcMojo;
import org.sonatype.flexmojos.plugin.compiler.attributes.converter.Module;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;

/**
 * <p>
 * Goal which compiles the SWF including all TEST libraries.
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.5
 * @goal test-swf
 * @requiresDependencyResolution test
 * @configurator flexmojos
 * @threadSafe
 */
public class TestMxmlcMojo
    extends MxmlcMojo
{
    @Override
    public String getClassifier()
    {
        return "test";
    }

    @Override
    public Module[] getModules()
    {
        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getIncludeLibraries()
    {
        return MavenUtils.getFiles( getDependencies( type( SWC ), anyOf( scope( INTERNAL ), scope( TEST ) ),
                                                     not( GLOBAL_MATCHER ) ) );
    }

}
