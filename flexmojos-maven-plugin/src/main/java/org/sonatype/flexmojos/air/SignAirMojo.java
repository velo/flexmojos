package org.sonatype.flexmojos.air;

import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.utilities.FileInterpolationUtil;

import com.adobe.air.ADT;
import com.adobe.air.AIRPackager;

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
    private String keystore;

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

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            Constructor<ADT> c = ADT.class.getDeclaredConstructor( AIRPackager.class );
            c.setAccessible( true );
            ADT adt = c.newInstance( new AIRPackager() );
            Method run = ADT.class.getDeclaredMethod( "run", String[].class );
            run.setAccessible( true );
            String[] args = getArgs();
            int result = (Integer) run.invoke( adt, (Object) args );
            if ( result != 0 )
            {
                throw new MojoFailureException( "Error generating AIR package " + result );
            }
        }
        catch ( MojoExecutionException e )
        {
            // do not handle
            throw e;
        }
        catch ( MojoFailureException e )
        {
            // do not handle
            throw e;
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error invoking AIR api, blame adobe for not providing a public API", e );
        }
    }

    @SuppressWarnings( "unchecked" )
    private String[] getArgs()
        throws MojoExecutionException
    {
        List<String> args = new ArrayList<String>();
        args.add( "-package" );
        args.add( "-storetype" );
        args.add( storetype );
        args.add( "-keystore" );
        args.add( keystore );
        args.add( "-storepass" );
        args.add( storepass );
        File output = new File( project.getBuild().getDirectory(), outputName );
        args.add( output.getAbsolutePath() );
        File xml = getAirDescriptor();
        args.add( xml.getAbsolutePath() );
        Set<Artifact> deps = project.getDependencyArtifacts();
        for ( Artifact artifact : deps )
        {
            if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
            {
                try
                {
                    FileUtils.copyFileToDirectory( artifact.getFile(), airOutput );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Failed to copy " + artifact, e );
                }
                args.add( artifact.getFile().getAbsolutePath() );
            }
        }

        System.out.println( args.toString().replace( ',', '\n' ) );

        return args.toArray( new String[0] );
    }

    @SuppressWarnings( "unchecked" )
    private File getAirDescriptor()
        throws MojoExecutionException
    {
        File output = null;
        Set<Artifact> deps = project.getDependencyArtifacts();
        for ( Artifact artifact : deps )
        {
            if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
            {
                output = artifact.getFile();
                break;
            }
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
