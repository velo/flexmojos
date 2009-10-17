package org.sonatype.flexmojos.asdoc;

import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.isMinVersionOK;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.splitVersion;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which generates documentation from the ActionScript sources in DITA format.
 * 
 * @goal dita-asdoc
 * @requiresDependencyResolution compile
 */
public class DitaAsdocMojo
    extends AsDocMojo
{

    /**
     * The output directory for the generated documentation.
     * 
     * @parameter default-value="${project.build.directory}/dita-asdoc"
     */
    protected File outputDirectory;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !isMinVersionOK( splitVersion( getCompilerVersion() ), splitVersion( "4.0.0.7219" ) ) )
        {
            getLog().warn( "Skipping Dita Asdoc.  Dita Asdoc is only available on Flex4." );
            return;
        }

        super.execute();
    }

    @Override
    protected void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        super.setUp();

        outputDirectory.mkdirs();
    }

    @Override
    protected void addExtraArgs( List<String> args )
    {
        super.addExtraArgs( args );

        args.add( "-output=" + outputDirectory.getAbsolutePath() );

        args.add( "-keep-xml=true" );
        args.add( "-skip-xsl=true" );
    }
}
