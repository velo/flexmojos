package info.flexmojos.install;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import eu.cedarsoft.utils.ZipCreator;

public abstract class AbstractInstallMojo
    extends AbstractMojo
{

    private static final String ADOBE_GROUP_ID = "com.adobe.flex";

    private static final String COMPILER_GROUP_ID = ADOBE_GROUP_ID + ".compiler";

    private static final String FRAMEWORK_GROUP_ID = ADOBE_GROUP_ID + ".framework";

    private static final String[] JARS = new String[] { "jar" };

    private static final String[] RSLS = new String[] { "swf", "swz" };

    private static final String[] SWCS = new String[] { "swc" };

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * File location where targeted Flex SDK is located
     * 
     * @parameter expression="${flex.sdk.folder}"
     */
    private File sdkFolder;

    /**
     * Flex SDK version. Recommend pattern:
     * <ul>
     * Append -FB3 suffix on Flexbuilder sdks
     * </ul>
     * <ul>
     * Append -LCDS suffix on LCDS sdks
     * </ul>
     * <BR>
     * Samples:
     * <ul>
     * 3.0.0.477
     * </ul>
     * <ul>
     * 3.0.0.477-FB3
     * </ul>
     * <ul>
     * 3.0.0.477-LCDS
     * </ul>
     * 
     * @parameter expression="${version}"
     */
    private String version;

    /**
     * @parameter expression="${overwriteLibFolder}"
     */
    private File overwriteLibFolder;

    /**
     * @parameter expression="${default.player.version}" default-value="9"
     */
    private String defaultPlayerVersion;

    public AbstractInstallMojo()
    {
        super();
    }

    /**
     * @param file
     * @param artifact
     */
    public abstract void installArtifact( File file, Artifact artifact );

    private Artifact createArtifact( File file, String groupId )
    {
        String artifactName = getArtifactName( file );
        String type = getExtension( file );
        String version = this.version;
        // special rule on playerglobal
        if ( "swc".equals( type ) && "playerglobal".equals( artifactName ) )
        {
            String parentName = file.getParentFile().getName();
            if ( "player".equals( parentName ) )
            {
                version = "9-" + version;
            }
            else
            {
                version = parentName + "-" + version;
            }
        }
        Artifact artifact = artifactFactory.createArtifact( groupId, artifactName, version, "compile", type );
        return artifact;
    }

    private void installFlexFrameworkArtifacts()
        throws MojoExecutionException
    {
        getLog().info( "Installing flex framework swcs" );
        Collection<Artifact> swcArtifacts = new ArrayList<Artifact>();

        File swcLibFolder = new File( sdkFolder, "frameworks/libs" );
        Collection<File> swcs = listFiles( swcLibFolder, SWCS, true );
        for ( File swc : swcs )
        {
            Artifact artifact = createArtifact( swc, FRAMEWORK_GROUP_ID );
            if ( "playerglobal".equals( artifact.getArtifactId() ) )
            {
                if ( artifact.getVersion().startsWith( defaultPlayerVersion ) )
                {
                    swcArtifacts.add( artifact );
                }
            }
            else
            {
                swcArtifacts.add( artifact );
            }
            installArtifact( swc, artifact );
            Artifact pomArtifact = createPomArtifact( artifact );
            generatePom( pomArtifact );
        }

        installResourceBundleArtifacts( swcArtifacts );
        installRslArtifacts();

        Collection<Artifact> flexArtifacts = filter( swcArtifacts, null, new String[] { "air*", "servicemonitor" } );
        Artifact flexSdk =
            artifactFactory.createArtifact( FRAMEWORK_GROUP_ID, "flex-framework", version, "compile", "pom" );
        generatePom( flexSdk, flexArtifacts );

        Collection<Artifact> airArtifacts = filter( swcArtifacts, null, new String[] { "player*" } );
        Artifact airSdk =
            artifactFactory.createArtifact( FRAMEWORK_GROUP_ID, "air-framework", version, "compile", "pom" );
        generatePom( airSdk, airArtifacts );

    }

    private Collection<Artifact> filter( Collection<Artifact> swcArtifacts, String[] include, String[] exclude )
    {
        if ( include == null )
        {
            include = new String[] { "*" };
        }

        if ( exclude == null )
        {
            exclude = new String[0];
        }

        List<Artifact> filtered = new ArrayList<Artifact>();
        for ( Artifact artifact : swcArtifacts )
        {
            for ( String wildcard : include )
            {
                if ( FilenameUtils.wildcardMatch( artifact.getArtifactId(), wildcard ) )
                {
                    filtered.add( artifact );
                    break;
                }
            }
        }

        for ( int i = filtered.size() - 1; i >= 0; i-- )
        {

            Artifact artifact = filtered.get( i );

            for ( String wildcard : exclude )
            {
                if ( FilenameUtils.wildcardMatch( artifact.getArtifactId(), wildcard ) )
                {
                    filtered.remove( artifact );
                    break;
                }
            }
        }
        return filtered;
    }

    private void installCompilerArtifacts()
        throws MojoExecutionException
    {
        getLog().info( "Installing flex compiler jars" );
        Set<Artifact> javaArtifacts = new HashSet<Artifact>();

        File libFolder;
        if ( overwriteLibFolder == null )
        {
            libFolder = new File( sdkFolder, "lib" );
        }
        else
        {
            libFolder = overwriteLibFolder;
        }

        if ( !libFolder.exists() )
        {
            throw new MojoExecutionException( "Java lib folder not fould: " + libFolder.getAbsolutePath() );
        }

        Collection<File> jars = listFiles( libFolder, JARS, false );

        for ( File jar : jars )
        {
            Artifact artifact = createArtifact( jar, COMPILER_GROUP_ID );
            javaArtifacts.add( artifact );
            installArtifact( jar, artifact );
            Artifact pomArtifact = createPomArtifact( artifact );
            generatePom( pomArtifact );
        }

        Artifact flexSdkLibs = artifactFactory.createArtifact( ADOBE_GROUP_ID, "compiler", version, "compile", "pom" );
        generatePom( flexSdkLibs, javaArtifacts );
    }

    private Artifact createPomArtifact( Artifact artifact )
    {
        Artifact pomArtifact =
            artifactFactory.createArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                                            "compile", "pom" );
        return pomArtifact;
    }

    private void installResourceBundleArtifacts( Collection<Artifact> swcArtifacts )
        throws MojoExecutionException
    {
        getLog().info( "Installing flex-sdk locale swcs" );
        File swcLocalesFolder = new File( sdkFolder, "frameworks/locale" );

        // create resource-bundle beacon
        installResourceBundleBeacon( swcLocalesFolder, swcArtifacts );

        File[] locales = swcLocalesFolder.listFiles( new FileFilter()
        {
            public boolean accept( File pathname )
            {
                return pathname.isDirectory();
            }
        } );
        for ( File localeFolder : locales )
        {
            Collection<File> localeSwcs = listFiles( localeFolder, SWCS, true );
            for ( File localeSwc : localeSwcs )
            {
                String artifactName = getResourceName( localeSwc );
                Artifact artifact =
                    artifactFactory.createArtifactWithClassifier( FRAMEWORK_GROUP_ID, artifactName, version, "rb.swc",
                                                                  localeFolder.getName() );
                installArtifact( localeSwc, artifact );
            }
        }
    }

    private void installResourceBundleBeacon( File swcLocalesFolder, Collection<Artifact> flexArtifacts )
        throws MojoExecutionException
    {
        Collection<File> localizedSwcs = listFiles( swcLocalesFolder, SWCS, true );
        Set<String> localizedSwcsNames = new HashSet<String>();
        for ( File localizedSwc : localizedSwcs )
        {
            String name = getResourceName( localizedSwc );
            localizedSwcsNames.add( name );
        }

        for ( String swcName : localizedSwcsNames )
        {
            Artifact artifact =
                artifactFactory.createArtifactWithClassifier( FRAMEWORK_GROUP_ID, swcName, version, "rb.swc", "" );

            File tempFile = createTempFile( swcName, "rb.swc" );
            try
            {
                FileUtils.copyURLToFile( getClass().getResource( "/rb.swc" ), tempFile );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to create beacon resource bundle. " + artifact, e );
            }
            installArtifact( tempFile, artifact );
            flexArtifacts.add( artifact );
        }
    }

    private void installRslArtifacts()
        throws MojoExecutionException
    {
        File rslsFolder = new File( sdkFolder, "frameworks/rsls" );
        if ( !rslsFolder.exists() )
        {
            getLog().warn( "Rsls folder not found: " + rslsFolder );
            return;
        }

        getLog().info( "Installing flex-sdk rsls" );
        Collection<File> rsls = listFiles( rslsFolder, RSLS, true );
        for ( File rsl : rsls )
        {
            String artifactName = getResourceName( rsl );
            String type = getExtension( rsl );
            Artifact artifact =
                artifactFactory.createArtifactWithClassifier( FRAMEWORK_GROUP_ID, artifactName, version, type, "" );
            installArtifact( rsl, artifact );
        }
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( sdkFolder == null )
        {
            throw new MojoExecutionException( "Flex SDK folder not defined." );
        }

        if ( !sdkFolder.exists() || !sdkFolder.isDirectory() )
        {
            throw new MojoExecutionException( "Flex SDK folder not found: " + sdkFolder.getAbsolutePath() );
        }

        installCompilerArtifacts();
        installAsdocTemplateArtifact();

        installFlexFrameworkArtifacts();

    }

    private void installAsdocTemplateArtifact()
        throws MojoExecutionException
    {

        File asdocTemplate = new File( sdkFolder, "asdoc/templates" );
        if ( !asdocTemplate.exists() )
        {
            throw new MojoExecutionException( "Asdoc template folder not fould: " + asdocTemplate.getAbsolutePath() );
        }

        File zipFile = createTempFile( "asdoc-template", ".zip" );

        ZipCreator zipper = new ZipCreator( zipFile );
        try
        {
            zipper.zip( asdocTemplate );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating asdoc-template", e );
        }

        Artifact artifact =
            artifactFactory.createArtifactWithClassifier( COMPILER_GROUP_ID, "asdoc", version, "zip", "template" );
        installArtifact( zipFile, artifact );

    }

    private File createTempFile( String prefix, String suffix )
        throws MojoExecutionException
    {
        try
        {
            File zipFile = File.createTempFile( prefix, suffix );
            zipFile.deleteOnExit();
            return zipFile;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to create temporary file" );
        }
    }

    private void generatePom( Artifact artifact )
        throws MojoExecutionException
    {
        generatePom( artifact, new HashSet<Artifact>() );
    }

    private void generatePom( Artifact artifact, Collection<Artifact> artifacts )
        throws MojoExecutionException
    {
        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( artifact.getGroupId() );
        model.setArtifactId( artifact.getArtifactId() );
        model.setVersion( artifact.getVersion() );
        model.setPackaging( artifact.getType() );
        model.setDescription( "POM was created from flex-mojos:install-sdk" );

        for ( Artifact artifactDependency : artifacts )
        {
            Dependency dep = new Dependency();
            dep.setGroupId( artifactDependency.getGroupId() );
            dep.setArtifactId( artifactDependency.getArtifactId() );
            dep.setVersion( artifactDependency.getVersion() );
            dep.setType( artifactDependency.getType() );
            model.addDependency( dep );
        }

        try
        {
            File tempFile = createTempFile( artifact.getArtifactId(), ".pom" );

            FileWriter fw = new FileWriter( tempFile );
            tempFile.deleteOnExit();
            new MavenXpp3Writer().write( fw, model );
            fw.flush();
            fw.close();
            installArtifact( tempFile, artifact );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error generating pom file: " + e.getMessage(), e );
        }

    }

    private String getArtifactName( File file )
    {
        String name = file.getName();
        name = name.substring( 0, name.lastIndexOf( '.' ) );
        return name;
    }

    private String getExtension( File file )
    {
        String name = file.getName();
        name = name.substring( name.lastIndexOf( '.' ) + 1 );
        return name;
    }

    private String getResourceName( File file )
    {
        String artifactName = getArtifactName( file );
        artifactName = artifactName.substring( 0, artifactName.lastIndexOf( '_' ) );
        return artifactName;
    }

    @SuppressWarnings( "unchecked" )
    private Collection<File> listFiles( File folder, String[] extensions, boolean recusive )
    {
        // Just a facade to avoid unchecked warnings
        return FileUtils.listFiles( folder, extensions, recusive );
    }

}