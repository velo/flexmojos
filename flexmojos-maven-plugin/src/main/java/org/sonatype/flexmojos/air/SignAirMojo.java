package org.sonatype.flexmojos.air;

import static org.sonatype.flexmojos.common.FlexExtension.AIR;
import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.sonatype.flexmojos.utilities.FileInterpolationUtil;

import com.adobe.air.AIRPackager;
import com.adobe.air.Listener;
import com.adobe.air.Message;

/**
 * @goal sign-air
 * @requiresDependencyResolution compile
 * @author Marvin Froeder
 */
public class SignAirMojo
    extends AbstractMojo
{

    /**
     * The type of keystore, determined by the keystore implementation.
     * 
     * @parameter default-value="pkcs12"
     */
    private String storetype;

    /**
     * @parameter default-value="${basedir}/src/main/resources/sign.p12"
     */
    private File keystore;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter default-value="${project.build.finalName}.air"
     */
    private String outputName;

    /**
     * @parameter default-value="${basedir}/src/main/resources/descriptor.xml"
     */
    private File descriptorTemplate;

    /**
     * @parameter
     * @required
     */
    private String storepass;

    /**
     * @parameter default-value="${project.build.directory}/air"
     */
    private File airOutput;

    /**
     * Plugin classpath.
     * 
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    protected List<Artifact> pluginClasspath;

    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        AIRPackager airPackager = new AIRPackager();
        try
        {
            File output = new File( project.getBuild().getDirectory(), outputName );
            airPackager.setOutput( output );
            airPackager.setDescriptor( getAirDescriptor() );

            KeyStore keyStore = KeyStore.getInstance( storetype );
            keyStore.load( new FileInputStream( keystore.getAbsolutePath() ), storepass.toCharArray() );
            String alias = keyStore.aliases().nextElement();
            airPackager.setPrivateKey( (PrivateKey) keyStore.getKey( alias, storepass.toCharArray() ) );
            airPackager.setSignerCertificate( keyStore.getCertificate( alias ) );
            airPackager.setCertificateChain( keyStore.getCertificateChain( alias ) );

            if ( project.getPackaging().equals( AIR ) )
            {
                Set<Artifact> deps = project.getDependencyArtifacts();
                for ( Artifact artifact : deps )
                {
                    if ( SWF.equals( artifact.getType() ) )
                    {
                        File source = artifact.getFile();
                        String path = source.getName();
                        getLog().debug( "  adding source " + source + " with path " + path );
                        airPackager.addSourceWithPath( source, path );
                    }
                }
            }
            else
            {
                File source = project.getArtifact().getFile();
                String path = source.getName();
                getLog().debug( "  adding source " + source + " with path " + path );
                airPackager.addSourceWithPath( source, path );
            }

            String path = project.getBuild().getFinalName() + "." + SWF;
            File source = new File( project.getBuild().getDirectory(), path );
            if ( source.exists() )
            {
                getLog().debug( "  adding source " + source + " with path " + path );
                airPackager.addSourceWithPath( source, path );
            }

            project.getArtifact().setFile( output );

            final List<Message> messages = new ArrayList<Message>();

            airPackager.setListener( new Listener()
            {
                public void message( Message message )
                {
                    messages.add( message );
                }

                public void progress( int soFar, int total )
                {
                    getLog().info( "  completed " + soFar + " of " + total );
                }
            } );

            airPackager.createAIR();

            if ( messages.size() > 0 )
            {
                for ( Message message : messages )
                {
                    getLog().error( "  " + message.errorDescription );
                }

                throw new MojoExecutionException( "Error creating AIR application" );
            }
            else
            {
                getLog().info( "  AIR package created: " + output.getAbsolutePath() );
            }
        }
        catch ( MojoExecutionException e )
        {
            // do not handle
            throw e;
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error invoking AIR api", e );
        }
        finally
        {
            airPackager.close();
        }
    }

    private File getAirDescriptor()
        throws MojoExecutionException
    {
        File output = null;
        if ( project.getPackaging().equals( AIR ) )
        {
            Set<Artifact> deps = project.getDependencyArtifacts();
            for ( Artifact artifact : deps )
            {
                if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
                {
                    output = artifact.getFile();
                    break;
                }
            }
        }
        else
        {
            output = project.getArtifact().getFile();
        }

        File dest = new File( airOutput, project.getBuild().getFinalName() + "-descriptor.xml" );
        try
        {
            FileInterpolationUtil.copyFile( descriptorTemplate, dest, Collections.singletonMap( "output",
                                                                                                output.getName() ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy air template", e );
        }
        return dest;
    }
}
