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
package org.sonatype.flexmojos.components.publisher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.components.publisher.filefilters.DirFilter;
import org.sonatype.flexmojos.components.publisher.filefilters.SuffixFilter;

public abstract class AbstractFlexSDKPublisher
    extends AbstractLogEnabled
    implements FlexSDKPublisher, Contextualizable
{

    private static final File TEMP_DIR = new File( System.getProperty( "java.io.tmpdir" ) );

    private static final Random RANDOM = new Random();

    public static final String[] JARS = new String[] { "jar" };

    public static final String[] RSLS = new String[] { "swf", "swz" };

    public static final String[] SWCS = new String[] { "swc" };

    public static final String[] CONFIGS = new String[] { "ser", "xml" };

    @Requirement
    protected ArtifactFactory artifactFactory;

    protected Context context;

    protected PlexusContainer plexusContainer;

    public void publish( File sdk, String version, int defaultPlayer, File overwriteLibFolder )
        throws PublishingException
    {
        getLogger().info( "Publishing flex SDK" );

        if ( sdk == null )
        {
            throw new PublishingException( "Flex SDK folder not defined." );
        }

        if ( !sdk.exists() )
        {
            throw new PublishingException( "Flex SDK folder not found: " + sdk.getAbsolutePath() );
        }

        File sdkFolder;
        File sdkBundle;
        File tempFile = createTempFile( "flex-sdk", version );
        if ( sdk.isDirectory() )
        {
            sdkFolder = sdk;

            try
            {
                ZipArchiver zipArchiver = getZipArchiver();
                zipArchiver.setDestFile( tempFile );
                zipArchiver.addDirectory( sdk );
                zipArchiver.createArchive();
            }
            catch ( Exception e )
            {
                throw new PublishingException( "Unable to create SDK bundle", e );
            }

            sdkBundle = tempFile;
        }
        else
        {
            sdkBundle = sdk;

            tempFile.mkdirs();
            try
            {
                ZipUnArchiver zipUnArchiver = getZipUnArchiver();
                zipUnArchiver.setSourceFile( sdk );
                zipUnArchiver.setDestDirectory( tempFile );
                zipUnArchiver.extract();
            }
            catch ( Exception e )
            {
                throw new PublishingException( "Unable to extract zipped SDK", e );
            }
            sdkFolder = tempFile;
        }

        sdk = null;
        tempFile = null;

        publishCompilerArtifacts( sdkFolder, overwriteLibFolder, version );

        publishFlexFrameworkArtifacts( sdkFolder, version, defaultPlayer );

        publishAsdocTemplateArtifact( sdkFolder, version );

        publishBundle( sdkBundle, version );
    }

    private Artifact createArtifact( File file, String groupId, String version )
    {
        String artifactName = getArtifactName( file );
        String type = getExtension( file );
        // special rule on playerglobal
        if ( "swc".equals( type ) && "playerglobal".equals( artifactName ) )
        {
            String parentName = file.getParentFile().getName();
            if ( "player".equals( parentName ) || "libs".equals( parentName ) )
            {
                version = "9-" + version;
            }
            else
            {
                version = parentName + "-" + version;
            }
        }
        Artifact artifact = artifactFactory.createArtifact( groupId, artifactName, version, "compile", type );
        artifact.setFile( file );
        artifact.setResolved( true );
        return artifact;
    }

    private void publishFlexFrameworkArtifacts( File sdkFolder, String version, int defaultPlayer )
        throws PublishingException
    {
        getLogger().info( "Installing flex framework swcs" );
        File swcLibFolder = new File( sdkFolder, "frameworks/libs" );
        Collection<Artifact> commonSwc = getCommonArtifacts( swcLibFolder, version );
        Collection<Artifact> airSwc = getAirArtifacts( swcLibFolder, version );
        Collection<Artifact> flexSwc = getFlexArtifacts( swcLibFolder, version, defaultPlayer );

        File swcLocaleFolder = new File( sdkFolder, "frameworks/locale" );
        Map<String, Artifact> resourceBundles = getResourceBundle( swcLocaleFolder, version );

        publishRslArtifacts( sdkFolder, version );
        Artifact configsArtifact = publishConfigFiles( sdkFolder, version );

        Collection<Artifact> flexArtifacts = new HashSet<Artifact>();
        flexArtifacts.addAll( commonSwc );
        flexArtifacts.addAll( flexSwc );
        flexArtifacts.add( configsArtifact );
        flexArtifacts.addAll( filterResourceBundles( resourceBundles, flexArtifacts ) );
        Artifact flexSdk =
            artifactFactory.createArtifact( FRAMEWORK_GROUP_ID, "flex-framework", version, "compile", "pom" );
        publishPom( flexSdk, flexArtifacts );

        Collection<Artifact> airArtifacts = new HashSet<Artifact>();
        airArtifacts.addAll( commonSwc );
        airArtifacts.addAll( airSwc );
        airArtifacts.add( configsArtifact );
        airArtifacts.addAll( filterResourceBundles( resourceBundles, airArtifacts ) );
        Artifact airSdk =
            artifactFactory.createArtifact( FRAMEWORK_GROUP_ID, "air-framework", version, "compile", "pom" );
        publishPom( airSdk, airArtifacts );

    }

    private Collection<Artifact> filterResourceBundles( Map<String, Artifact> resourceBundles,
                                                        Collection<? extends Artifact> swcs )
    {
        Collection<Artifact> rbs = new ArrayList<Artifact>();
        for ( Artifact artifact : swcs )
        {
            Artifact rb = resourceBundles.get( artifact.getArtifactId() );
            if ( rb != null )
            {
                rbs.add( rb );
            }
        }
        return rbs;
    }

    private Map<String, Artifact> getResourceBundle( File localesDir, String version )
        throws PublishingException
    {

        File beaconFile = createTempFile( "swc_resource_bundle", "rb.swc" );
        try
        {
            FileUtils.copyURLToFile( getClass().getResource( "/rb.swc" ), beaconFile );
        }
        catch ( IOException e )
        {
            throw new PublishingException( "Failed to create resource bundle beacon.", e );
        }

        File[] locales = localesDir.listFiles( DirFilter.INSTANCE );
        Map<String, Artifact> artifacts = new HashMap<String, Artifact>();
        for ( File localeDir : locales )
        {
            File[] localeSwcs = localeDir.listFiles( new SuffixFilter( SWCS ) );

            for ( File localeSwc : localeSwcs )
            {
                String artifactName = getResourceName( localeSwc );

                Artifact artifact =
                    artifactFactory.createArtifactWithClassifier( FRAMEWORK_GROUP_ID, artifactName, version, "rb.swc",
                                                                  localeDir.getName() );
                artifact.setFile( localeSwc );
                artifact.setResolved( true );
                publishArtifact( artifact );

                if ( !artifacts.containsKey( artifactName ) )
                {
                    Artifact beaconArtifact =
                        artifactFactory.createArtifactWithClassifier( FRAMEWORK_GROUP_ID, artifactName, version,
                                                                      "rb.swc", "" );
                    beaconArtifact.setFile( beaconFile );
                    beaconArtifact.setResolved( true );
                    artifacts.put( artifactName, beaconArtifact );
                    publishArtifact( beaconArtifact );
                }
            }

        }

        return artifacts;
    }

    private Collection<Artifact> getFlexArtifacts( File swcLibFolder, String version, int defaultPlayer )
        throws PublishingException
    {
        File playerLibFolder = new File( swcLibFolder, "player" );
        if ( !playerLibFolder.exists() )
        {
            return Collections.emptyList();
        }
        String[] swcPaths = FileUtils.getFilesFromExtension( playerLibFolder.getAbsolutePath(), new String[] { "swc" } );
        File[] swcs = new File[swcPaths.length];
        for ( int i = 0; i < swcPaths.length; i++ )
        {
            String path = swcPaths[i];
            swcs[i] = new File( path );
        }

        Collection<Artifact> artifacts = getArtifacts( swcs, version );
        for ( Iterator<Artifact> iterator = artifacts.iterator(); iterator.hasNext(); )
        {
            Artifact artifact = iterator.next();
            if ( "playerglobal".equals( artifact.getArtifactId() )
                && !artifact.getVersion().startsWith( String.valueOf( defaultPlayer ) ) )
            {
                iterator.remove();
            }
        }
        return artifacts;
    }

    private Collection<Artifact> getAirArtifacts( File swcLibFolder, String version )
        throws PublishingException
    {
        File airLibFolder = new File( swcLibFolder, "air" );

        if ( !airLibFolder.exists() )
        {
            return Collections.emptyList();
        }

        File[] swcs = airLibFolder.listFiles( new SuffixFilter( SWCS ) );
        return getArtifacts( swcs, version );
    }

    private Collection<Artifact> getCommonArtifacts( File swcLibFolder, String version )
        throws PublishingException
    {
        File[] swcs = swcLibFolder.listFiles( new SuffixFilter( SWCS ) );
        return getArtifacts( swcs, version );
    }

    private Collection<Artifact> getArtifacts( File[] swcs, String version )
        throws PublishingException
    {
        Collection<Artifact> artifacts = new HashSet<Artifact>();
        for ( File swc : swcs )
        {
            Artifact artifact = createArtifact( swc, FRAMEWORK_GROUP_ID, version );
            publishArtifact( artifact );
            artifacts.add( artifact );
            Artifact pomArtifact = createPomArtifact( artifact );
            publishArtifact( pomArtifact );
        }

        return artifacts;
    }

    private void publishCompilerArtifacts( File sdkFolder, File overwriteLibFolder, String version )
        throws PublishingException
    {
        getLogger().info( "Installing flex compiler jars" );
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
            throw new PublishingException( "Java lib folder not fould: " + libFolder.getAbsolutePath() );
        }

        File[] jars = libFolder.listFiles( new SuffixFilter( JARS ) );

        for ( File jar : jars )
        {
            Artifact artifact = createArtifact( jar, COMPILER_GROUP_ID, version );
            javaArtifacts.add( artifact );
            Artifact pomArtifact = createPomArtifact( artifact );
            publishArtifact( artifact );
            publishArtifact( pomArtifact );
        }

        Artifact flexSdkLibs = artifactFactory.createArtifact( ADOBE_GROUP_ID, "compiler", version, "compile", "pom" );
        publishPom( flexSdkLibs, javaArtifacts );
    }

    private void publishPom( Artifact pomArtifact, Collection<Artifact> artifacts )
        throws PublishingException
    {
        File pomFile = generatePom( pomArtifact, artifacts );
        pomArtifact.setFile( pomFile );
        pomArtifact.setResolved( true );
        publishArtifact( pomArtifact );
    }

    private Artifact createPomArtifact( Artifact artifact )
        throws PublishingException
    {
        Artifact pomArtifact =
            artifactFactory.createArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                                            "compile", "pom" );
        File pomFile = generatePom( artifact );
        pomArtifact.setFile( pomFile );
        pomArtifact.setResolved( true );
        return pomArtifact;
    }

    private void publishRslArtifacts( File sdkFolder, String version )
        throws PublishingException
    {
        File rslsFolder = new File( sdkFolder, "frameworks/rsls" );
        if ( !rslsFolder.exists() )
        {
            getLogger().warn( "Rsls folder not found: " + rslsFolder );
            return;
        }

        getLogger().info( "Installing flex-sdk rsls" );
        File[] rsls = rslsFolder.listFiles( new SuffixFilter( RSLS ) );
        for ( File rsl : rsls )
        {
            String artifactName = getResourceName( rsl );
            String type = getExtension( rsl );
            Artifact artifact =
                artifactFactory.createArtifactWithClassifier( FRAMEWORK_GROUP_ID, artifactName, version, type, "" );
            artifact.setFile( rsl );
            artifact.setResolved( true );
            publishArtifact( artifact );
        }
    }

    private void publishBundle( File sdkBundle, String version )
        throws PublishingException
    {
        Artifact artifact =
            artifactFactory.createArtifactWithClassifier( ADOBE_GROUP_ID, "flex-sdk", version, "zip", "bundle" );
        artifact.setFile( sdkBundle );
        artifact.setResolved( true );
        publishArtifact( artifact );
    }

    protected abstract void publishArtifact( Artifact artifact )
        throws PublishingException;

    private Artifact publishConfigFiles( File sdkFolder, String version )
        throws PublishingException
    {

        File frameworks = new File( sdkFolder, "frameworks" );
        File[] configFiles = frameworks.listFiles( new SuffixFilter( CONFIGS ) );

        File zipFile = createTempFile( "config-files", ".zip" );

        try
        {
            ZipArchiver zipArchiver = getZipArchiver();

            for ( File configFile : configFiles )
            {
                zipArchiver.addFile( configFile, configFile.getName() );
            }
            zipArchiver.setDestFile( zipFile );
            zipArchiver.createArchive();
        }
        catch ( Exception e )
        {
            throw new PublishingException( "Unable to create configuration file", e );
        }

        Artifact artifact =
            artifactFactory.createArtifactWithClassifier( FRAMEWORK_GROUP_ID, "framework", version, "zip", "configs" );
        artifact.setFile( zipFile );
        artifact.setResolved( true );
        publishArtifact( artifact );

        return artifact;
    }

    private void publishAsdocTemplateArtifact( File sdkFolder, String version )
        throws PublishingException
    {

        File asdocTemplate = new File( sdkFolder, "asdoc/templates" );
        if ( !asdocTemplate.exists() )
        {
            throw new PublishingException( "Asdoc template folder not fould: " + asdocTemplate.getAbsolutePath() );
        }

        File zipFile = createTempFile( "asdoc-template", ".zip" );

        try
        {
            ZipArchiver zipArchiver = getZipArchiver();
            zipArchiver.setDestFile( zipFile );
            zipArchiver.addDirectory( asdocTemplate );
            zipArchiver.createArchive();
        }
        catch ( Exception e )
        {
            throw new PublishingException( "Error creating asdoc-template", e );
        }

        Artifact artifact =
            artifactFactory.createArtifactWithClassifier( COMPILER_GROUP_ID, "asdoc", version, "zip", "template" );
        artifact.setFile( zipFile );
        artifact.setResolved( true );
        publishArtifact( artifact );

    }

    private File createTempFile( String prefix, String suffix )
    {
        File tempFile = new File( TEMP_DIR, prefix + "-" + Long.toHexString( RANDOM.nextInt( 4 ) ) + "-" + suffix );
        return tempFile;
    }

    private File generatePom( Artifact artifact )
        throws PublishingException
    {
        return generatePom( artifact, new HashSet<Artifact>() );
    }

    private File generatePom( Artifact artifact, Collection<Artifact> artifacts )
        throws PublishingException
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
            dep.setClassifier( artifactDependency.getClassifier() );
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
            return tempFile;
        }
        catch ( IOException e )
        {
            throw new PublishingException( "Error generating pom file: " + e.getMessage(), e );
        }

    }

    private String getArtifactName( File file )
    {
        return FileUtils.removeExtension( file.getName() );
    }

    private String getExtension( File file )
    {
        return FileUtils.getExtension( file.getName() );
    }

    private String getResourceName( File file )
    {
        String artifactName = getArtifactName( file );
        artifactName = artifactName.substring( 0, artifactName.lastIndexOf( '_' ) );
        return artifactName;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        this.context = context;
        this.plexusContainer = (PlexusContainer) context.get( "plexus" );
    }

    protected ZipArchiver getZipArchiver()
        throws ComponentLookupException
    {
        return (ZipArchiver) plexusContainer.lookup( Archiver.ROLE, "zip" );
    }

    protected ZipUnArchiver getZipUnArchiver()
        throws ComponentLookupException
    {
        return (ZipUnArchiver) plexusContainer.lookup( UnArchiver.ROLE, "zip" );
    }

}
