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

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.eclipse.EclipseConfigFile;
import org.apache.maven.plugin.eclipse.EclipsePlugin;
import org.apache.maven.plugin.ide.IdeDependency;
import org.apache.maven.plugin.ide.IdeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.sonatype.flexmojos.utilities.MavenUtils;
import org.sonatype.flexmojos.utilities.PathUtil;
import org.sonatype.flexmojos.utilities.SourceFileResolver;

/**
 * Generates Flex Builder configuration files for SWC and SWF projects.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.0
 * @extendsPlugin eclipse
 * @extendsGoal eclipse
 * @goal flexbuilder
 * @requiresDependencyResolution
 */
public class FlexbuilderMojo
    extends EclipsePlugin
{

    private static final String APPLICATION_NATURE = "com.adobe.flexbuilder.project.flexnature";

    private static final String LIBRARY_NATURE = "com.adobe.flexbuilder.project.flexlibnature";

    private static final String ACTIONSCRIPT_NATURE = "com.adobe.flexbuilder.project.actionscriptnature";

    private static final String FLEXBUILDER_BUILD_COMMAND = "com.adobe.flexbuilder.project.flexbuilder";

    // TODO get from M2EclipseMojo
    protected static final String M2ECLIPSE_NATURE = "org.maven.ide.eclipse.maven2Nature";

    protected static final String M2ECLIPSE_BUILD_COMMAND = "org.maven.ide.eclipse.maven2Builder";

    private static final String SWC = "swc";

    private static final String SWF = "swf";

    private static final String RB_SWC = "rb.swc";

    private static final String[] SDK_SOURCES =
        { "automation", "flex", "framework", "haloclassic", "rpc", "utilities" };

    /**
     * @parameter default-value="true" expression="${enableM2e}"
     */
    private boolean enableM2e;

    /**
     * @parameter default-value="true" expression="${enableFlexBuilderBuildCommand}"
     */
    private boolean enableFlexBuilderBuildCommand;

    /**
     * Implies enableM2e=true
     * 
     * @parameter default-value="false" expression="${useM2Home}"
     */
    private boolean useM2Home;

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
     * When true resources are compiled into Application or Library. When false resources are compiled into separated
     * Application or Library files. If not defined no resourceBundle generation is done
     * 
     * @parameter
     * @deprecated
     */
    private Boolean mergeResourceBundle;

    /**
     * Sets the locales that the compiler uses to replace <code>{locale}</code> tokens that appear in some configuration
     * values. This is equivalent to using the <code>compiler.locale</code> option of the mxmlc or compc compilers. <BR>
     * Usage:
     * 
     * <pre>
     * &lt;compiledLocales&gt;
     *    &lt;locale&gt;en_US&lt;/locale&gt;
     *    &lt;locale&gt;pt_BR&lt;/locale&gt;
     *    &lt;locale&gt;es_ES&lt;/locale&gt;
     * &lt;/compiledLocales&gt;
     * </pre>
     * 
     * @parameter
     */
    protected String[] compiledLocales;

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
     * List of path elements that form the roots of ActionScript class hierarchies.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;sourcePaths&gt;
     *    &lt;path&gt;${baseDir}/src/main/flex&lt;/path&gt;
     * &lt;/sourcePaths&gt;
     * </pre>
     * 
     * By default use Maven source and resources folders.
     * 
     * @parameter
     */
    protected File[] sourcePaths;

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
     * Define the base path to locate resouce bundle files Accept some special tokens:
     * 
     * <pre>
     * {locale}     - replace by locale name
     * </pre>
     * 
     * @parameter default-value="${basedir}/src/main/locales/{locale}"
     */
    protected String resourceBundlePath;

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

    @Override
    public boolean setup()
        throws MojoExecutionException
    {
        String packaging = project.getPackaging();
        if ( !( SWF.equals( packaging ) || SWC.equals( packaging ) ) )
        {
            return false;
        }

        File classpathEntries = new File( project.getBasedir(), ".classpath" );
        if ( classpathEntries.exists() )
        {
            // java nature breaks flex nature.
            classpathEntries.delete();
            new File( project.getBasedir(), ".project" ).delete();
        }

        // Just as precaution, in case someone adds a 'source' not in the natural order for strings
        Arrays.sort( SDK_SOURCES );

        return super.setup();
    }

    @Override
    public void writeConfiguration( IdeDependency[] deps )
        throws MojoExecutionException
    {
        super.writeConfiguration( deps );

        String packaging = project.getPackaging();

        if ( SWF.equals( packaging ) || SWC.equals( packaging ) )
        {
            writeAsProperties( packaging, deps );
        }

        if ( SWF.equals( packaging ) )
        {
            writeFlexProperties();
        }
        else if ( SWC.equals( packaging ) )
        {
            writeFlexLibProperties();
        }

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

        if ( enableM2e || useM2Home )
        {
            getProjectnatures().add( M2ECLIPSE_NATURE );
        }
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultBuilders( String packaging )
    {
        super.fillDefaultBuilders( packaging );

        if ( ( SWF.equals( packaging ) || SWC.equals( packaging ) ) && enableFlexBuilderBuildCommand )
        {
            getBuildcommands().add( FLEXBUILDER_BUILD_COMMAND );
        }

        if ( enableM2e || useM2Home )
        {
            getBuildcommands().add( M2ECLIPSE_BUILD_COMMAND );
        }
    }

    private void writeFlexLibProperties()
        throws MojoExecutionException
    {
        VelocityContext context = new VelocityContext();
        // TODO if no includeClasses set put them all there
        context.put( "flexClasses", includeClasses );
        // TODO
        // context.put( "includeFiles", files );

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
                String template = IdeUtils.PROJECT_NAME_DEFAULT_TEMPLATE; // TODO
                // http://jira.codehaus.org/browse/MECLIPSE-519
                String projectName = IdeUtils.getProjectName( template, ideDependency );
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
                                           false, false, false, art.getFile(), art.getType(), false, null, 1 );

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

        if ( SWF.equals( packaging ) )
        {
            File sourceFile =
                SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), this.sourceFile,
                                                      project.getGroupId(), project.getArtifactId() );

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

    private Collection<String> getRelativeSources()
    {
        Collection<String> sourceRoots = getSourceRoots();

        Collection<String> sources = new HashSet<String>();
        for ( String sourceRoot : sourceRoots )
        {
            File source = new File( sourceRoot );
            if ( source.isAbsolute() )
            {
                String relative = PathUtil.getRelativePath( project.getBasedir(), source );
                sources.add( relative.replace( '\\', '/' ) );
            }
            else
            {
                sources.add( sourceRoot );
            }
        }

        return sources;
    }

    private Collection<String> getAbsolutePaths( File[] sourcePaths )
    {
        Collection<String> paths = new HashSet<String>();
        for ( File file : sourcePaths )
        {
            paths.add( file.getAbsolutePath() );
        }
        return paths;
    }

    @SuppressWarnings( "unchecked" )
    private Collection<String> getSourceRoots()
    {
        if ( sourcePaths != null )
        {
            return getAbsolutePaths( sourcePaths );
        }

        Set<String> sources = new HashSet<String>();
        List<String> sourceRoots;

        if ( project.getExecutionProject() != null )
        {
            sourceRoots = project.getExecutionProject().getCompileSourceRoots();
        }
        else
        {
            sourceRoots = project.getCompileSourceRoots();
        }
        sources.addAll( sourceRoots );

        List<String> testRoots;
        if ( project.getExecutionProject() != null )
        {
            testRoots = project.getExecutionProject().getTestCompileSourceRoots();
        }
        else
        {
            testRoots = project.getTestCompileSourceRoots();
        }
        sources.addAll( testRoots );

        for ( Resource resource : (List<Resource>) project.getBuild().getResources() )
        {
            sources.add( resource.getDirectory() );
        }
        for ( Resource resource : (List<Resource>) project.getBuild().getTestResources() )
        {
            sources.add( resource.getDirectory() );
        }

        for ( Iterator<String> iterator = sources.iterator(); iterator.hasNext(); )
        {
            String path = iterator.next();
            if ( !new File( path ).exists() )
            {
                iterator.remove();
            }
        }

        if ( Boolean.TRUE.equals( mergeResourceBundle ) || compiledLocales != null )
        {
            sources.add( resourceBundlePath );
        }

        return sources;
    }

    private String getPlainLocales()
    {
        Collection<String> locales = getLocales();
        String buf = plain( locales );
        return buf;
    }

    private String plain( Collection<String> locales )
    {
        StringBuilder buf = new StringBuilder();
        for ( String locale : locales )
        {
            if ( buf.length() != 0 )
            {
                buf.append( ' ' );
            }
            buf.append( locale );
        }
        return buf.toString();
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
    protected void setupExtras()
        throws MojoExecutionException
    {

        String packaging = project.getPackaging();

        if ( !SWF.equals( packaging ) && !SWC.equals( packaging ) )
        {
            return;
        }

        EclipseConfigFile utfConfig = new EclipseConfigFile();
        utfConfig.setName( ".settings/org.eclipse.core.resources.prefs" );
        utfConfig.setContent( getSettingsContent() );

        try
        {
            Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses( "additionalConfig", getClass() );
            field.setAccessible( true );
            EclipseConfigFile[] originalConfig = (EclipseConfigFile[]) field.get( this );
            EclipseConfigFile[] configs = new EclipseConfigFile[] { utfConfig };

            configs = (EclipseConfigFile[]) ArrayUtils.addAll( configs, originalConfig );
            field.set( this, configs );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error settings project to UTF-8", e );
        }
    }

    private String getSettingsContent()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( '#' ).append( new Date().toString() ).append( '\n' );
        sb.append( "eclipse.preferences.version=1" ).append( '\n' );
        sb.append( "encoding/<project>=UTF-8" ).append( '\n' );
        return sb.toString();
    }
}
