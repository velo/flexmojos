/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonatype.flexmojos.air;

import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;
import static org.sonatype.flexmojos.common.FlexExtension.AIR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.sonatype.flexmojos.utilities.FileInterpolationUtil;

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

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            // look for EMMA dependency in this plugin classpath
            final Map<String, Artifact> pluginArtifactMap = ArtifactUtils.artifactMapByVersionlessId( pluginClasspath );
            Artifact adtArtifact = (Artifact) pluginArtifactMap.get( "com.adobe.flex:adt" );

            if ( adtArtifact == null )
            {
                throw new MojoExecutionException(
                                                  "Failed to find 'adt' artifact in plugin dependencies.  Be sure of adding it with compile scope!" );
            }

            Commandline cmd = new Commandline();
            cmd.setExecutable( "java" );
            cmd.setWorkingDirectory( project.getPackaging().equals( AIR )
                ? airOutput.getAbsolutePath()
                : project.getBuild().getDirectory() );
            cmd.createArgument().setValue( "-jar" );
            cmd.createArgument().setValue( adtArtifact.getFile().getAbsolutePath() );

            String[] args = getArgs();
            cmd.addArguments( args );

            StreamConsumer consumer = new StreamConsumer()
            {
                public void consumeLine( String line )
                {
                    getLog().info( "  " + line );
                }
            };

            getLog().info( cmd.toString() );

            int result = CommandLineUtils.executeCommandLine( cmd, consumer, consumer );

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
        args.add( keystore.getAbsolutePath() );
        args.add( "-storepass" );
        args.add( storepass );
        File output = new File( project.getBuild().getDirectory(), outputName );
        args.add( output.getAbsolutePath() );
        File xml = getAirDescriptor();
        args.add( xml.getAbsolutePath() );
        if ( project.getPackaging().equals( AIR ) )
        {
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
                    args.add( new File( airOutput, artifact.getFile().getName() ).getAbsolutePath() );
                }
            }
        }
        else
        {
            args.add( project.getArtifact().getFile().getAbsolutePath() );
        }

        project.getArtifact().setFile( output );

        return args.toArray( new String[args.size()] );
    }

    @SuppressWarnings( "unchecked" )
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
