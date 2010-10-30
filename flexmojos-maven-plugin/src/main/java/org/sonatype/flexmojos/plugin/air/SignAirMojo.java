package org.sonatype.flexmojos.plugin.air;

import static org.sonatype.flexmojos.plugin.common.FlexExtension.AIR;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.AIRN;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.ANE;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.APK;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.DEB;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.DMG;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.EXE;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.RPM;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWF;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.TAR_GZ;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.ZIP;
import static org.sonatype.flexmojos.util.PathUtil.path;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.repository.legacy.resolver.transform.SnapshotTransformation;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.air.packager.FlexmojosAIRNPackager;
import org.sonatype.flexmojos.plugin.air.packager.FlexmojosAIRPackager;
import org.sonatype.flexmojos.plugin.air.packager.FlexmojosANEPackager;
import org.sonatype.flexmojos.plugin.air.packager.FlexmojosAPKPackager;
import org.sonatype.flexmojos.plugin.air.packager.FlexmojosDEBPackager;
import org.sonatype.flexmojos.plugin.air.packager.FlexmojosDMGPackager;
import org.sonatype.flexmojos.plugin.air.packager.FlexmojosEXEPackager;
import org.sonatype.flexmojos.plugin.air.packager.FlexmojosRPMPackager;
import org.sonatype.flexmojos.plugin.air.packager.IPackager;
import org.sonatype.flexmojos.plugin.utilities.FileInterpolationUtil;
import org.sonatype.flexmojos.util.PathUtil;

import com.adobe.air.Listener;
import com.adobe.air.Message;

/**
 * @goal sign-air
 * @phase package
 * @requiresDependencyResolution compile
 * @author Marvin Froeder
 */
public class SignAirMojo
    extends AbstractMavenMojo
{

    private static String TIMESTAMP_NONE = "none";

    /**
     * @parameter default-value="${project.build.directory}/air"
     */
    private File airOutput;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     * 
     * @parameter expression="${flexmojos.classifier}"
     */
    private String classifier;

    /**
     * @parameter default-value="${basedir}/src/main/resources/descriptor.xml"
     * @required
     */
    private File descriptorTemplate;

    /**
     * Ideally Adobe would have used some parseable token, not a huge pass-phrase on the descriptor output. They did
     * prefer to reinvent wheel, so more work to all of us.
     * 
     * @parameter expression="${flexmojos.flexbuilderCompatibility}"
     */
    private boolean flexBuilderCompatibility;

    /**
     * Include specified files in AIR package.
     * 
     * @parameter
     */
    private List<String> includeFiles;

    /**
     * Include specified files or directories in AIR package.
     * 
     * @parameter
     */
    private FileSet[] includeFileSets;

    /**
     * @parameter default-value="${basedir}/src/main/resources/sign.p12"
     */
    private File keystore;

    /**
     * Valid values: 'air', 'dmg', 'exe', 'rpm' and 'deb'
     * <p>
     * Default-value = 'air'
     * 
     * @parameter
     */
    private List<String> packages = Arrays.asList( AIR );

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @component
     * @required
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

    /**
     * @parameter
     * @required
     */
    private String storepass;

    /**
     * The type of keystore, determined by the keystore implementation.
     * 
     * @parameter default-value="pkcs12"
     */
    private String storetype;

    /**
     * Strip artifact version during copy of dependencies.
     * 
     * @parameter default-value="false"
     */
    private boolean stripVersion;

    /**
     * The URL for the timestamp server. If 'none', no timestamp will be used.
     * 
     * @parameter
     */
    private String timestampURL;

    private void addSourceWithPath( IPackager packager, String directory, String includePath )
        throws MojoFailureException
    {
        if ( includePath == null )
        {
            throw new MojoFailureException( "Cannot include a null file" );
        }

        // get file from output directory to allow filtered resources
        File includeFile = new File( directory, includePath );
        if ( !includeFile.isFile() )
        {
            throw new MojoFailureException( "Include files only accept files as parameters: " + includePath );
        }

        // don't include the app descriptor or the cert
        if ( path( includeFile ).equals( path( this.descriptorTemplate ) )
            || path( includeFile ).equals( path( this.keystore ) ) )
        {
            return;
        }

        getLog().debug( "  adding source " + includeFile + " with path " + includePath );
        packager.addSourceWithPath( includeFile, includePath );
    }

    private void appendArtifacts( IPackager packager, Collection<Artifact> deps )
    {
        for ( Artifact artifact : deps )
        {
            if ( SWF.equals( artifact.getType() ) )
            {
                File source = artifact.getFile();
                String path = source.getName();
                if ( stripVersion && path.contains( artifact.getVersion() ) )
                {
                    path = path.replace( "-" + artifact.getVersion(), "" );
                }
                getLog().debug( "  adding source " + source + " with path " + path );
                packager.addSourceWithPath( source, path );
            }
        }
    }

    private void doPackage( String packagerName, IPackager packager )
        throws MojoExecutionException
    {
        try
        {
            String c = this.classifier == null ? "" : "-" + this.classifier;
            File output =
                new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + c + "." + packagerName );
            packager.setOutput( output );
            packager.setDescriptor( getAirDescriptor() );

            KeyStore keyStore = KeyStore.getInstance( storetype );
            keyStore.load( new FileInputStream( keystore.getAbsolutePath() ), storepass.toCharArray() );
            String alias = keyStore.aliases().nextElement();
            packager.setPrivateKey( (PrivateKey) keyStore.getKey( alias, storepass.toCharArray() ) );
            packager.setSignerCertificate( keyStore.getCertificate( alias ) );
            packager.setCertificateChain( keyStore.getCertificateChain( alias ) );
            if ( this.timestampURL != null )
            {
                packager.setTimestampURL( TIMESTAMP_NONE.equals( this.timestampURL ) ? null : this.timestampURL );
            }

            String packaging = project.getPackaging();
            if ( AIR.equals( packaging ) )
            {
                appendArtifacts( packager, project.getDependencyArtifacts() );
                appendArtifacts( packager, project.getAttachedArtifacts() );
            }
            else if ( SWF.equals( packaging ) )
            {
                File source = project.getArtifact().getFile();
                String path = source.getName();
                getLog().debug( "  adding source " + source + " with path " + path );
                packager.addSourceWithPath( source, path );
            }
            else
            {
                throw new MojoFailureException( "Unexpected project packaging " + packaging );
            }

            if ( includeFiles == null && includeFileSets == null )
            {
                includeFileSets = resources.toArray( new FileSet[0] );
            }

            if ( includeFiles != null )
            {
                for ( final String includePath : includeFiles )
                {
                    String directory = project.getBuild().getOutputDirectory();
                    addSourceWithPath( packager, directory, includePath );
                }
            }

            if ( includeFileSets != null )
            {
                for ( FileSet set : includeFileSets )
                {
                    DirectoryScanner scanner;
                    if ( set instanceof Resource )
                    {
                        scanner = scan( (Resource) set );
                    }
                    else
                    {
                        scanner = scan( set );
                    }

                    String[] files = scanner.getIncludedFiles();
                    for ( String path : files )
                    {
                        addSourceWithPath( packager, set.getDirectory(), path );
                    }
                }
            }

            if ( classifier != null )
            {
                projectHelper.attachArtifact( project, packagerName, classifier, output );
            }
            else if ( SWF.equals( packaging ) )
            {
                projectHelper.attachArtifact( project, packagerName, output );
            }
            else
            {
                if ( AIR.equals( packagerName ) && AIR.equals( packaging ) )
                {
                    project.getArtifact().setFile( output );
                }
                else
                {
                    projectHelper.attachArtifact( project, packagerName, output );
                }
            }

            final List<Message> messages = new ArrayList<Message>();

            try
            {
                packager.setListener( new Listener()
                {
                    public void message( final Message message )
                    {
                        messages.add( message );
                    }

                    public void progress( final int soFar, final int total )
                    {
                        getLog().info( "  completed " + soFar + " of " + total );
                    }
                } );
            }
            catch ( NullPointerException e )
            {
                // this is a ridiculous workaround, but I have no means to prevent the NPE nor to check if it will
                // happen on AIR 2.5
                if ( getLog().isDebugEnabled() )
                {
                    getLog().error( e.getMessage() );
                }
            }

            packager.createPackage();

            if ( messages.size() > 0 )
            {
                for ( final Message message : messages )
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
            packager.close();
        }
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Map<String, IPackager> packagers = getPackagers();
        for ( Entry<String, IPackager> packager : packagers.entrySet() )
        {
            doPackage( packager.getKey(), packager.getValue() );
        }
    }

    private File getAirDescriptor()
        throws MojoExecutionException
    {
        File output = getOutput();

        String version;
        if ( project.getArtifact().isSnapshot() )
        {
            String timestamp = SnapshotTransformation.getUtcDateFormatter().format( new Date() );
            version = project.getVersion().replace( "SNAPSHOT", timestamp );
        }
        else
        {
            version = project.getVersion();
        }

        File dest = new File( airOutput, project.getBuild().getFinalName() + "-descriptor.xml" );
        try
        {
            Map<String, String> props = new HashMap<String, String>();
            props.put( "output", output.getName() );
            props.put( "version", version );

            FileInterpolationUtil.copyFile( descriptorTemplate, dest, props );

            if ( flexBuilderCompatibility )
            {
                // Workaround Flexbuilder/Flashbuilder weirdness
                String str = FileUtils.fileRead( dest );
                str =
                    str.replace( "[This value will be overwritten by Flex Builder in the output app.xml]",
                                 output.getName() );
                str =
                    str.replace( "[This value will be overwritten by Flash Builder in the output app.xml]",
                                 output.getName() );
                FileUtils.fileWrite( PathUtil.path( dest ), str );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy air template", e );
        }

        return dest;
    }

    protected File getRuntimeDir( String classifier, String type )
    {
        return getUnpackedArtifact( "com.adobe.adl", "runtime", getAirTarget(), classifier, type );
    }

    private File getNaiDir( String classifier, String type )
        throws MojoExecutionException
    {
        return getUnpackedArtifact( "com.adobe.adl", "nai", getAirTarget(), classifier, type );
    }

    private File getOutput()
    {
        File output = null;
        if ( project.getPackaging().equals( AIR ) )
        {
            List<Artifact> attach = project.getAttachedArtifacts();
            for ( Artifact artifact : attach )
            {
                if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
                {
                    return artifact.getFile();
                }
            }
            Set<Artifact> deps = project.getDependencyArtifacts();
            for ( Artifact artifact : deps )
            {
                if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
                {
                    return artifact.getFile();
                }
            }
        }
        else
        {
            output = project.getArtifact().getFile();
        }
        return output;
    }

    private Map<String, IPackager> getPackagers()
        throws MojoExecutionException
    {
        getLog().info( "Creating the following packagers: " + packages.toString() );

        Map<String, IPackager> packs = new LinkedHashMap<String, IPackager>();
        if ( packages.contains( AIR ) )
        {
            packs.put( AIR, new FlexmojosAIRPackager() );
        }
        if ( packages.contains( DMG ) )
        {
            packs.put( DMG, new FlexmojosDMGPackager( getNaiDir( "mac", TAR_GZ ), getRuntimeDir( "mac", TAR_GZ ) ) );
        }
        if ( packages.contains( EXE ) )
        {
            packs.put( EXE, new FlexmojosEXEPackager( getNaiDir( null, ZIP ), getRuntimeDir( null, ZIP ) ) );
        }
        if ( packages.contains( RPM ) )
        {
            packs.put( RPM, new FlexmojosRPMPackager( getNaiDir( "linux", TAR_GZ ), getRuntimeDir( "linux", TAR_GZ ) ) );
        }
        if ( packages.contains( DEB ) )
        {
            packs.put( DEB, new FlexmojosDEBPackager( getNaiDir( "linux", TAR_GZ ), getRuntimeDir( "linux", TAR_GZ ) ) );
        }
        if ( packages.contains( APK ) )
        {
             packs.put( APK, new FlexmojosAPKPackager() );
        }
        if ( packages.contains( AIRN ) )
        {
            packs.put( AIRN, new FlexmojosAIRNPackager() );
        }
        if ( packages.contains( ANE ) )
        {
            packs.put( ANE, new FlexmojosANEPackager() );
        }

        if ( packages.size() != packs.size() )
        {
            getLog().error( "Invalid package found, valid values are: 'air', 'dmg', 'exe', 'rpm' and 'deb', but got "
                                + packages.toString() );
        }

        if ( packs.isEmpty() )
        {
            getLog().debug( "Packagers is empty or contains invalid packagers, using AIR" );
            packs.put( AIR, new FlexmojosAIRPackager() );
        }
        return packs;
    }

}
