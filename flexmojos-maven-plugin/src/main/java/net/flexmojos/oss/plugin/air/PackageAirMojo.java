/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.flexmojos.oss.plugin.air;

import net.flexmojos.oss.plugin.AbstractMavenMojo;
import net.flexmojos.oss.plugin.air.packager.*;
import net.flexmojos.oss.plugin.utilities.FileInterpolationUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static ch.lambdaj.Lambda.filter;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.*;
import static net.flexmojos.oss.plugin.common.FlexExtension.AIR;
import static net.flexmojos.oss.plugin.common.FlexExtension.SWF;
import static net.flexmojos.oss.util.PathUtil.file;


/**
 * @author Marvin Froeder
 * @goal package-air
 * @phase package
 * @requiresDependencyResolution compile
 */
public class PackageAirMojo
        extends AbstractMavenMojo {

    /**
     * @component role="net.flexmojos.oss.plugin.air.packager.PackagerFactory"
     */
    private PackagerFactory packagerFactory;

    /**
     * Target platform the application should be packaged.
     * Default "air" will create an ".air" file that can be run on any
     * system for which an AIR runtime is available. Alternate values are:
     * - air (default)
     * - windows
     * - mac
     * - linux-debian
     * - linux-rpm
     * - android
     * - ios
     *
     * @parameter default-value="air"
     * @required
     */
    private String targetPlatform;

    /**
     * If set to true the created output will contain the AIR runtime.
     * This will dramatically increase the size of the output (about 9MB)
     * but the application will be able to be run without having to install
     * the AIR runtime.
     * <p/>
     * Only used in Android and Desktop (Windows, Mac, Linux) output.
     *
     * @parameter default-value="false"
     * @required
     */
    private boolean includeCaptiveRuntime;

    /**
     * Directory in which the temporary AIR sdk will be created.
     *
     * @parameter default-value="${project.build.directory}/adt"
     */
    private File workDir;

    /**
     * @parameter default-value="${basedir}/src/main/air/descriptor.xml"
     * @required
     */
    private File descriptorTemplate;

    /**
     * Additional properties to substitute into the air descriptor.
     *
     * @parameter
     */
    private Map<String, String> descriptorTemplateProperties;

    /**
     * Path to the keyfile used for signing the AIR application.
     *
     * @parameter default-value="${basedir}/src/main/air/sign.p12"
     */
    private File storefile;

    /**
     * Password for accessing the certificate in the keystore.
     *
     * @parameter
     */
    private String storepass;

    /**
     * The type of keystore, determined by the keystore implementation.
     *
     * @parameter default-value="pkcs12"
     */
    private String storetype;

    /**
     * Path to the provisioning profile document.
     *
     * @parameter
     */
    private File iosProvisioningProfile;

    /**
     * Defines the type of ios packaging. Possible values are:
     * - ipa-ad-hoc
     * - ipa-app-store (default)
     * - ipa-debug
     * - ipa-test
     * - ipa-debug-interpreter
     * - ipa-debug-interpreter-simulator
     * - ipa-test-interpreter
     * - ipa-test-interpreter-simulator
     *
     * @parameter default-value="ipa-app-store"
     */
    private String iosPackagingType;

    /**
     * Path to the ios platform sdk.
     * <p/>
     * If not provided, per default the files provided with the AIR sdk is used.
     * Example Path:
     * /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS8.4.sdk
     *
     * @parameter
     */
    private File iosPlatformSdk;

    /**
     * Classifier added to the output file name allowing to produce multiple
     * Variants of one artifact in one build.
     *
     * @parameter
     */
    private String classifier;

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

    public void fmExecute()
            throws MojoExecutionException, MojoFailureException {
        PackagingRequest packagingRequest = new PackagingRequest();
        packagingRequest.setLog(getLog());
        Resolver resolver = new Resolver(repositorySystem, localRepository, remoteRepositories);
        packagingRequest.setResolver(resolver);
        Set<Artifact> airCompilerArtifacts = new LinkedHashSet<Artifact>(filter(groupId("com.adobe.air.compiler"),
                pluginArtifacts));
        packagingRequest.setArtifacts(airCompilerArtifacts);

        packagingRequest.setTargetPlatform(targetPlatform);
        packagingRequest.setIncludeCaptiveRuntime(includeCaptiveRuntime);
        packagingRequest.setClassifier(classifier);

        includeFileSets = resources.toArray(new FileSet[resources.size()]);
        if (includeFileSets != null) {
            for (FileSet set : includeFileSets) {
                DirectoryScanner scanner;
                if (set instanceof Resource) {
                    scanner = scan((Resource) set);
                } else {
                    scanner = scan(set);
                }

                if (scanner != null) {
                    File directory = file(set.getDirectory(), project.getBasedir());

                    String[] files = scanner.getIncludedFiles();
                    for (String file : files) {
                       packagingRequest.addIncludedFile(directory.getAbsolutePath(), file);
                    }
                }
            }
        }

        packagingRequest.setWorkDir(workDir);
        packagingRequest.setBuildDir(getBuildDirectory());
        packagingRequest.setFinalName(project.getBuild().getFinalName());

        packagingRequest.setStorefile(storefile);
        packagingRequest.setStoretype(storetype);
        packagingRequest.setStorepass(storepass);

        packagingRequest.setIosProvisioningProfile(iosProvisioningProfile);
        packagingRequest.setIosPlatformSdk(iosPlatformSdk);
        packagingRequest.setIosPackagingType(iosPackagingType);

        Artifact swfArtifact = getSwfArtifact();
        if (swfArtifact == null) {
            throw new MojoExecutionException("Unable to find SWF artifact.");
        }
        packagingRequest.setInputFile(swfArtifact.getFile());
        packagingRequest.setDescriptorFile(getDescriptorFile());

        Packager packager = packagerFactory.getPackager(packagingRequest);
        if (packager == null) {
            throw new MojoExecutionException("Unable to get packager for current configuration.");
        }

        packager.setRequest(packagingRequest);
        try {
            packager.prepare();
        } catch (PackagingException e) {
            throw new MojoExecutionException("An error occurred while preparing packager of type " +
                    packager.getClass().getName(), e);
        }

        try {
            File output = packager.execute();
            String fileType = output.getName().substring(output.getName().lastIndexOf(".") + 1);
            if (classifier != null) {
                projectHelper.attachArtifact(project, fileType, classifier, output);
            } else if (SWF.equals(packaging)) {
                projectHelper.attachArtifact(project, fileType, output);
            } else if (AIR.equals(fileType) && AIR.equals(packaging)) {
                project.getArtifact().setFile(output);
            } else {
                projectHelper.attachArtifact(project, fileType, output);
            }
        } catch (PackagingException e) {
            throw new MojoExecutionException("An error occurred while executing packager of type " +
                    packager.getClass().getName(), e);
        }
    }

    protected Artifact getSwfArtifact() throws MojoExecutionException {
        if ((project.getActiveProfiles() != null) && SWF.equals(project.getArtifact().getType())) {
            return project.getArtifact();
        }
        for (Artifact attachedArtifact : project.getAttachedArtifacts()) {
            if (SWF.equalsIgnoreCase(attachedArtifact.getType())) {
                return attachedArtifact;
            }
        }
        Set<Artifact> swfArtifacts = getDependencies(type(SWF), scope("compile"));
        if (swfArtifacts.size() == 1) {
            return swfArtifacts.iterator().next();
        } else if (swfArtifacts.size() > 1) {
            throw new MojoExecutionException("More than one SWF artifact with scope compile added as dependency.");
        }
        return null;
    }

    protected File getDescriptorFile() throws MojoExecutionException {
        File descriptorFile = new File(getBuildDirectory(), project.getBuild().getFinalName() + "-descriptor.xml");

        ConcurrentHashMap<String, String> templateProps = new ConcurrentHashMap<String, String>();
        if (descriptorTemplateProperties != null) {
            templateProps.putAll(descriptorTemplateProperties);
        }
        templateProps.putIfAbsent("output", getSwfArtifact().getFile().getName());
        templateProps.putIfAbsent("version", project.getVersion());

        try {
            FileInterpolationUtil.copyFile(descriptorTemplate, descriptorFile, templateProps);

            return descriptorFile;
        } catch (IOException e) {
            throw new MojoExecutionException("Error preparing descriptor.", e);
        }
    }


/*    private void addSourceWithPath(BasePackager packager, File directory, String includePath )
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

    private void appendArtifacts(BasePackager packager, Collection<Artifact> deps )
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

    protected void doPackage( String fileEnding, BasePackager packager )
        throws MojoExecutionException
    {
        final List<Message> messages = new ArrayList<Message>();
        String c = this.classifier == null ? "" : "-" + this.classifier;
        File output =
                new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + c + "." + fileEnding );

        try
        {
            KeyStore keyStore = KeyStore.getInstance( storetype );
            keyStore.load( new FileInputStream( keystore.getAbsolutePath() ), storepass.toCharArray() );
            String alias = keyStore.aliases().nextElement();
            PrivateKey key = (PrivateKey) keyStore.getKey( alias, storepass.toCharArray() );
            packager.setPrivateKey( key );

            packager.setOutput( output );
            packager.setDescriptor( getAirDescriptor() );

            Certificate certificate = keyStore.getCertificate( alias );
            packager.setSignerCertificate( certificate );
            Certificate[] certificateChain = keyStore.getCertificateChain( alias );
            packager.setCertificateChain( certificateChain );
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
                includeFileSets = resources.toArray( new FileSet[resources.size()] );
            }

            if ( includeFiles != null )
            {
                for ( final String includePath : includeFiles )
                {
                    File directory = file( project.getBuild().getOutputDirectory() );
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

                    if( scanner != null ) {
                        File directory = file(set.getDirectory(), project.getBasedir());

                        String[] files = scanner.getIncludedFiles();
                        for (String path : files) {
                            addSourceWithPath(packager, directory, path);
                        }
                    }
                }
            }

            if ( classifier != null )
            {
                projectHelper.attachArtifact( project, fileEnding, classifier, output );
            }
            else if ( SWF.equals( packaging ) )
            {
                projectHelper.attachArtifact( project, fileEnding, output );
            }
            else
            {
                if ( AIR.equalsIgnoreCase( fileEnding ) && AIR.equals( packaging ) )
                {
                    project.getArtifact().setFile( output );
                }
                else
                {
                    projectHelper.attachArtifact( project, fileEnding, output );
                }
            }


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
        }
        catch ( MojoExecutionException e )
        {
            // do not handle
            throw e;
        }
        catch ( Exception e )
        {
            if ( getLog().isDebugEnabled() )
            {
                getLog().error( e.getMessage(), e );
            }
            throw new MojoExecutionException( "Error invoking AIR api", e );
        }
        finally
        {
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
            packager.close();
        }
    }

    protected File getAirDescriptor()
            throws MojoExecutionException
    {
        File output = getOutput();

        String version;
        if ( project.getArtifact().isSnapshot() )
        {
            version =
                    project.getVersion().replace( "SNAPSHOT", new SimpleDateFormat( "yyyyMMdd.HHmmss" ).format( new Date() ) );
        }
        else
        {
            version = project.getVersion();
        }

        File dest = new File( airOutput, project.getBuild().getFinalName() + "-descriptor.xml" );
        try
        {
            ConcurrentMap<String, String> props = getDescriptorProperties();
            props.putIfAbsent("output", output.getName());
            props.putIfAbsent("version", version);

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

    private ConcurrentMap<String, String> getDescriptorProperties() {
        final ConcurrentHashMap<String, String> templateProps = new ConcurrentHashMap<String, String>();
        if (descriptorTemplateProperties != null) {
            templateProps.putAll(descriptorTemplateProperties);
        }
        return templateProps;
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

    protected Artifact getAirPackagingResources()
    {
        Matcher<? extends Artifact>[] packagingResourcesMatchers = new  Matcher[] {
                groupId( "com.adobe.air.compiler" ), artifactId( "adt" ), classifier( "android" ),
                type( "zip" )
        };
        Artifact packagingResources =
                getDependency(packagingResourcesMatchers);

        // not on dependency list, trying to resolve it manually
        if ( packagingResources == null )
        {
            if(getIsAirProject()) {
                packagingResources = resolve("com.adobe.air.compiler", "adt", getAirVersion(), "android", "zip");
            }
        }
        return packagingResources;
    }*/

}
