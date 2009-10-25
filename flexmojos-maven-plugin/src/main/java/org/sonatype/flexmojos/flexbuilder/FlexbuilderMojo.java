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
package org.sonatype.flexmojos.flexbuilder;

import static org.sonatype.flexmojos.common.FlexExtension.AIR;
import static org.sonatype.flexmojos.common.FlexExtension.RB_SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.ide.IdeDependency;
import org.apache.maven.plugin.ide.IdeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.sonatype.flexmojos.test.util.PathUtil;
import org.sonatype.flexmojos.utilities.MavenUtils;
import org.sonatype.flexmojos.utilities.SourceFileResolver;

/**
 * Generates Flex Builder configuration files for SWC and SWF projects.
 *
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.0
 * @extendsPlugin eclipse
 * @extendsGoal eclipse
 * @goal flexbuilder
 * @requiresDependencyResolution compile
 */
public class FlexbuilderMojo
    extends AbstractIdeMojo
{

    static final String APPLICATION_NATURE = "com.adobe.flexbuilder.project.flexnature";

    static final String LIBRARY_NATURE = "com.adobe.flexbuilder.project.flexlibnature";

    static final String ACTIONSCRIPT_NATURE = "com.adobe.flexbuilder.project.actionscriptnature";

    static final String FLEXBUILDER_AIR_NATURE = "com.adobe.flexbuilder.apollo.apollonature";

    static final String FLEXBUILDER_BUILD_COMMAND = "com.adobe.flexbuilder.project.flexbuilder";

    static final String AIR_BUILD_COMMAND = "com.adobe.flexbuilder.apollo.apollobuilder";

    static final String[] SDK_SOURCES = { "automation", "flex", "framework", "haloclassic", "rpc", "utilities" };

    /**
     * @parameter default-value="true" expression="${enableFlexBuilderBuildCommand}"
     */
    boolean enableFlexBuilderBuildCommand;

    /**
     * @parameter default-value="false" expression="${generateHtmlWrapper}"
     */
    private boolean generateHtmlWrapper;

    /* start duplicated */
    /**
     * @parameter default-value="9.0.0"
     */
    private String targetPlayer;

    /**
     * Turn on generation of accessible SWFs.
     *
     * @parameter default-value="false"
     */
    private boolean accessible;

    /**
     * Customize the outputFolderPath of the Eclipse FlexBuilder Compiler.
     *
     * @parameter default-value="bin-debug"
     */
    private String flexBuilderOutputFolderPath;

    /**
     * Run the AS3 compiler in strict error checking mode.
     *
     * @parameter default-value="true"
     */
    private boolean strict;

    /**
     * Verifies the RSL loaded has the same digest as the RSL specified when the application was compiled. This is
     * equivalent to using the <code>verify-digests</code> option in the mxmlc compiler.
     *
     * @parameter default-value="true"
     */
    private boolean verifyDigests;

    /**
     * Run the AS3 compiler in a mode that detects legal but potentially incorrect code
     *
     * @parameter default-value="true"
     */
    private boolean showWarnings;

    /**
     * Sets the locales that the compiler uses to replace <code>{locale}</code> tokens that appear in some configuration
     * values. This is equivalent to using the <code>compiler.locale</code> option of the mxmlc or compc compilers. <BR>
     * Usage:
     *
     * <pre>
     * &lt;locales&gt;
     *    &lt;locale&gt;en_US&lt;/locale&gt;
     *    &lt;locale&gt;pt_BR&lt;/locale&gt;
     *    &lt;locale&gt;es_ES&lt;/locale&gt;
     * &lt;/locales&gt;
     * </pre>
     *
     * @parameter
     * @deprecated
     */
    protected String[] locales;

    /**
     * Default locale for libraries. This is useful to non localized applications, just to define swc.rb locale
     *
     * @parameter default-value="en_US"
     */
    private String defaultLocale;

    /**
     * This is the equilvalent of the <code>include-sources</code> option of the compc compiler.<BR>
     * Usage:
     *
     * <pre>
     * &lt;includeSources&gt;
     *   &lt;sources&gt;${baseDir}/src/main/flex&lt;/sources&gt;
     * &lt;/includeSources&gt;
     * </pre>
     *
     * @parameter
     */
    protected File[] includeSources;

    /**
     * This is the equilvalent of the <code>include-classes</code> option of the compc compiler.<BR>
     * Usage:
     *
     * <pre>
     * &lt;includeClassses&gt;
     *   &lt;class&gt;foo.Bar&lt;/class&gt;
     * &lt;/includeClasses&gt;
     * </pre>
     *
     * @parameter
     */
    protected String[] includeClasses;

    /**
     * The file to be compiled. The path must be relative to the source folder.
     *
     * @parameter
     */
    protected String sourceFile;

    /**
     * Additional application files. The paths must be relative to the source folder.
     *
     * @parameter
     * @alias "applications"
     */
    protected List<String> additionalApplications;

    /**
     * List of css files that will be compiled into swfs within Eclipse. The path must be relative to the base directory
     * of the project. Usage:
     *
     * <pre>
     * &lt;buildCssFiles&amp;gt
     *     &lt;path&gt;src/style/main.css&lt;path&gt;
     * &lt;/buildCssFiles&gt;
     * </pre>
     *
     * @parameter
     */
    protected String[] buildCssFiles;

    /* end duplicated */

    /**
     * @component
     */
    private VelocityComponent velocityComponent;

    /**
     * Sets the location of the Flex Data Services service configuration file. This is equivalent to using the
     * <code>compiler.services</code> option of the mxmlc and compc compilers. If not define will look inside resources
     * directory for services-config.xml
     *
     * @parameter
     */
    private File services;

    /**
     * The greeting to display.
     *
     * @parameter services default-value="true"
     */
    private boolean incremental;

    @Override
    public void writeConfiguration( IdeDependency[] deps )
        throws MojoExecutionException
    {
        super.writeConfiguration( deps );

        init();

        String packaging = project.getPackaging();

        if ( SWF.equals( packaging ) || SWC.equals( packaging ) || AIR.equals( packaging ) )
        {
            writeAsProperties( packaging, deps );
        }

        if ( SWF.equals( packaging )  || AIR.equals( packaging ) )
        {
            writeFlexProperties();
        }
        else if ( SWC.equals( packaging ) )
        {
            writeFlexLibProperties();
        }

    }

    @SuppressWarnings( "unchecked" )
    private void init()
    {
        if ( services == null )
        {
            List<Resource> resources = project.getBuild().getResources();
            for ( Resource resource : resources )
            {
                File cfg = new File( resource.getDirectory(), "services-config.xml" );
                if ( cfg.exists() )
                {
                    services = cfg;
                    break;
                }
            }
        }

    }

    private void writeFlexLibProperties()
        throws MojoExecutionException
    {
        VelocityContext context = new VelocityContext();
        context.put( "flexClasses", includeClasses );

        runVelocity( "/templates/flexbuilder/flexLibProperties.vm", ".flexLibProperties", context );
    }

    private void writeFlexProperties()
        throws MojoExecutionException
    {
        VelocityContext context = new VelocityContext();

        runVelocity( "/templates/flexbuilder/flexProperties.vm", ".flexProperties", context );
    }

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    @SuppressWarnings( "unchecked" )
    protected List remoteRepositories;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    protected Collection<IdeDependency> getDependencies( IdeDependency[] ideDependencies )
        throws MojoExecutionException
    {
        List<IdeDependency> dependencies = new ArrayList<IdeDependency>( Arrays.asList( ideDependencies ) );
        List<IdeDependency> extraRbs = resolveResourceBundles( dependencies );
        for ( IdeDependency ideDependency : dependencies )
        {
            if ( ideDependency.isReferencedProject() )
            {
                String projectName = ideDependency.getEclipseProjectName();
                // /todolist-lib/bin-debug/todolist-lib.swc
                ideDependency.setFile( new File( "/" + projectName + "/bin-debug/" + projectName + ".swc" ) );
                ideDependency.setSourceAttachment( new File( "/" + projectName + "/src/main/flex/" ) );
            }
            else
            {

                String ideDependencyScope = null;

                if ( ideDependency.isSystemScoped() )
                {
                    ideDependencyScope = Artifact.SCOPE_SYSTEM;
                }
                else if ( ideDependency.isTestDependency() )
                {
                    ideDependencyScope = Artifact.SCOPE_TEST;
                }
                else if ( ideDependency.isProvided() )
                {
                    ideDependencyScope = Artifact.SCOPE_PROVIDED;
                }

                Artifact art =
                    artifactFactory.createArtifact( ideDependency.getGroupId(), ideDependency.getArtifactId(),
                                                    ideDependency.getVersion(), ideDependencyScope,
                                                    ideDependency.getType() );

                art = MavenUtils.resolveArtifact( project, art, resolver, localRepository, remoteRepositories );

                if ( useM2Home )
                {
                    ideDependency.setFile( new File(
                                                     ideDependency.getFile().getPath().replace(
                                                                                                localRepository.getBasedir(),
                                                                                                "${M2_HOME}" ) ) );
                }
                else
                {
                    ideDependency.setFile( ideDependency.getFile().getAbsoluteFile() );
                }

                if ( Arrays.binarySearch( SDK_SOURCES, ideDependency.getArtifactId() ) >= 0 )
                {
                    ideDependency.setSourceAttachment( new File( "${PROJECT_FRAMEWORKS}/projects/"
                        + ideDependency.getArtifactId() + "/src" ) );
                }
            }
        }

        Set<IdeDependency> result = new LinkedHashSet<IdeDependency>();
        result.addAll( dependencies );
        result.addAll( extraRbs );
        return result;
    }

    private List<IdeDependency> resolveResourceBundles( List<IdeDependency> dependencies )
        throws MojoExecutionException
    {
        Collection<String> locales = getLocales();

        List<IdeDependency> extraRbs = new ArrayList<IdeDependency>();

        for ( Iterator<IdeDependency> it = dependencies.iterator(); it.hasNext(); )
        {
            IdeDependency dependency = it.next();
            if ( "playerglobal".equals( dependency.getArtifactId() ) || "airglobal".equals( dependency.getArtifactId() ) )
            {
                it.remove();
            }
            else if ( SWC.equals( dependency.getType() ) )
            {
                continue;
            }
            else if ( RB_SWC.equals( dependency.getType() ) )
            {
                for ( String locale : locales )
                {
                    Artifact art =
                        artifactFactory.createArtifactWithClassifier( dependency.getGroupId(),
                                                                      dependency.getArtifactId(),
                                                                      dependency.getVersion(), dependency.getType(),
                                                                      locale );

                    art = MavenUtils.resolveArtifact( project, art, resolver, localRepository, remoteRepositories );

                    IdeDependency dep =
                        new IdeDependency( art.getGroupId(), art.getArtifactId(), art.getVersion(),
                                           art.getClassifier(), false, Artifact.SCOPE_TEST.equals( art.getScope() ),
                                           false, false, false, art.getFile(), art.getType(), false, null, 1 ,
                                           IdeUtils.getProjectName(IdeUtils.PROJECT_NAME_DEFAULT_TEMPLATE, art));

                    if ( useM2Home )
                    {
                        dep.setFile( new File( dep.getFile().getPath().replace( localRepository.getBasedir(),
                                                                                "${M2_HOME}" ) ) );
                    }

                    extraRbs.add( dep );
                }
                it.remove();
            }
            else
            {
                it.remove();
            }
        }
        return extraRbs;
    }

    private Collection<String> getLocales()
    {
        Set<String> localesList = new HashSet<String>();
        if ( locales != null )
        {
            localesList.addAll( Arrays.asList( locales ) );
        }
        if ( compiledLocales != null )
        {
            localesList.addAll( Arrays.asList( compiledLocales ) );
        }
        if ( localesList.isEmpty() )
        {
            localesList.add( defaultLocale );
        }
        return localesList;
    }

    @SuppressWarnings( "unchecked" )
    private void writeAsProperties( String packaging, IdeDependency[] ideDependencies )
        throws MojoExecutionException
    {
        VelocityContext context = new VelocityContext();
        context.put( "useM2Home", useM2Home );
        context.put( "dependencies", getDependencies( ideDependencies ) );
        context.put( "mainSources", getMainSources() );
        context.put( "flexBuilderOutputFolderPath", flexBuilderOutputFolderPath );
        context.put( "targetPlayer", targetPlayer );
        context.put( "accessible", accessible );
        context.put( "strict", strict );
        context.put( "useApolloConfig", useApolloConfig( ideDependencies ) );
        context.put( "verifyDigests", verifyDigests );
        context.put( "showWarnings", showWarnings );

        String additionalCompilerArguments = "";
        if ( ( compiledLocales != null && compiledLocales.length > 0 ) || !SWC.equals( packaging ) )
        {
            additionalCompilerArguments += " -locale " + getPlainLocales();
        }

        if ( services != null )
        {
            additionalCompilerArguments += " -services " + services.getAbsolutePath();
        }

        if ( incremental )
        {
            additionalCompilerArguments += " --incremental ";
        }

        if ( SWF.equals( packaging ) || AIR.equals( packaging ) )
        {
            File sourceFile =
                SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), this.sourceFile,
                                                      project.getGroupId(), project.getArtifactId() );

            if( sourceFile == null )
            {
                throw new MojoExecutionException( "Could not find main application! " +
                        "(Hint: Try to create a MXML file below your source root)" );
            }

            context.put( "mainApplication", sourceFile.getName() );
            getAllApplications().add( 0, sourceFile.getName() );
            context.put( "applications", getAllApplications() );
            context.put( "generateHtmlWrapper", generateHtmlWrapper );
            context.put( "cssfiles", buildCssFiles );
        }
        else if ( SWC.equals( packaging ) )
        {
            context.put( "mainApplication", project.getArtifactId() + ".as" );
            if ( includeClasses == null && includeSources == null )
            {
                additionalCompilerArguments += " -include-sources " + plain( getSourceRoots() );
            }
            else if ( includeSources != null )
            {
                additionalCompilerArguments += " -include-sources " + getPlainSources();
            }
            context.put( "generateHtmlWrapper", false );
        }
        context.put( "additionalCompilerArguments", additionalCompilerArguments.trim() );
        context.put( "sources", getRelativeSources() );
        context.put( "PROJECT_FRAMEWORKS", "${PROJECT_FRAMEWORKS}" ); // flexbuilder required
        context.put( "libraryPathDefaultLinkType", getLibraryPathDefaultLinkType() ); // change flex framework linkage
        runVelocity( "/templates/flexbuilder/actionScriptProperties.vm", ".actionScriptProperties", context );
    }

    private boolean useApolloConfig( IdeDependency[] ideDependencies )
        throws MojoExecutionException
    {
        Collection<IdeDependency> deps = getDependencies( ideDependencies );
        for ( IdeDependency dep : deps )
        {
            if ( "airglobal".equals( dep.getArtifactId() ) )
            {
                return true;
            }
        }
        return false;
    }

    private List<String> getAllApplications()
    {
        if ( additionalApplications == null )
        {
            additionalApplications = new ArrayList<String>( 10 );
        }

        return additionalApplications;
    }

    private String getMainSources()
    {
        String mainSources =
            PathUtil.getRelativePath( project.getBasedir(), new File( project.getBuild().getSourceDirectory() ) ).replace(
                                                                                                                           '\\',
                                                                                                                           '/' );
        return mainSources;
    }

    private String getPlainSources()
    {
        Collection<String> sources = new ArrayList<String>();
        for ( File source : includeSources )
        {
            sources.add( PathUtil.getRelativePath( project.getBasedir(), source ) );
        }
        return plain( sources );
    }

    private String getPlainLocales()
    {
        Collection<String> locales = getLocales();
        String buf = plain( locales );
        return buf;
    }

    /**
     * Looks for the Flex framework dependency and determines result depending on specified scope.
     *
     * @return "1" if framework is merged into code, or "3" if its a runtime shared library
     * @throws MojoExecutionException if framework dependency can not be found
     */
    private String getLibraryPathDefaultLinkType()
        throws MojoExecutionException
    {
        final Artifact flexArtifact = resolveFlexFrameworkArtifact();
        final boolean isRsl = "rsl".equals( flexArtifact.getScope() );
        return isRsl ? "3" : "1";
    }

    @SuppressWarnings( "unchecked" )
    private Artifact resolveFlexFrameworkArtifact()
        throws MojoExecutionException
    {
        Set<Artifact> artifacts = project.getArtifacts();

        if ( artifacts == null )
        {
            throw new MojoExecutionException( "Could not find Flex Framework! Dependencies were not yet resolved!" );
        }

        for ( Artifact artifact : artifacts )
        {
            if ( "com.adobe.flex.framework".equals( artifact.getGroupId() )
                && "framework".equals( artifact.getArtifactId() ) && "swc".equals( artifact.getType() ) )
            {
                getLog().debug(
                                "Found Flex framework artifact. Scope: [" + artifact.getScope() + "]; " + "Version: ["
                                    + artifact.getVersion() + "]" );
                return artifact;
            }
        }

        throw new MojoExecutionException( "Could not find Flex Framework! Not included as dependency!" );
    }

    private void runVelocity( String templateName, String fileName, VelocityContext context )
        throws MojoExecutionException
    {

        Writer writer = null;
        try
        {
            Template template = velocityComponent.getEngine().getTemplate( templateName );
            writer = new FileWriter( new File( project.getBasedir(), fileName ) );
            template.merge( context, writer );
            writer.flush();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error writting " + fileName, e );
        }
        finally
        {
            if ( writer != null )
            {
                IOUtil.close( writer );
            }
        }

    }

    @Override
    public boolean setup()
        throws MojoExecutionException
    {
        // Just as precaution, in case someone adds a 'source' not in the natural order for strings
        Arrays.sort( SDK_SOURCES );

        return super.setup();
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultNatures( String packaging )
    {
        super.fillDefaultNatures( packaging );

        if ( SWF.equals( packaging ) )
        {
            getProjectnatures().add( APPLICATION_NATURE );
            getProjectnatures().add( ACTIONSCRIPT_NATURE );
        }

        if ( SWC.equals( packaging ) )
        {
            getProjectnatures().add( LIBRARY_NATURE );
            getProjectnatures().add( ACTIONSCRIPT_NATURE );
        }

        if ( AIR.equals( packaging ) )
        {
            getProjectnatures().add( APPLICATION_NATURE );
            getProjectnatures().add( ACTIONSCRIPT_NATURE );
            getProjectnatures().add( FLEXBUILDER_AIR_NATURE );
        }
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultClasspathContainers( String packaging )
    {
        super.fillDefaultClasspathContainers( packaging );

        if ( ( SWF.equals( packaging ) || SWC.equals( packaging ) || AIR.equals( packaging ) ) && enableFlexBuilderBuildCommand )
        {
            getBuildcommands().add( FLEXBUILDER_BUILD_COMMAND );
        }

        if ( AIR.equals( packaging ) && enableFlexBuilderBuildCommand )
        {
            getBuildcommands().add( AIR_BUILD_COMMAND );
        }
    }
}
