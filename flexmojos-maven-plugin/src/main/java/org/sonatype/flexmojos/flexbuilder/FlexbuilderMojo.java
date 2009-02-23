/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
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

    /**
     * @parameter default-value="true" expression="${enableM2e}"
     */
    private boolean enableM2e;

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
     * The file to be compiled. The path must be relative with source folder
     * 
     * @parameter
     */
    protected String sourceFile;

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

        if ( enableM2e )
        {
            getProjectnatures().add( M2ECLIPSE_NATURE );
        }
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultBuilders( String packaging )
    {
        super.fillDefaultBuilders( packaging );

        if ( SWF.equals( packaging ) || SWC.equals( packaging ) )
        {
            getBuildcommands().add( FLEXBUILDER_BUILD_COMMAND );
        }

        if ( enableM2e )
        {
            getBuildcommands().add( M2ECLIPSE_BUILD_COMMAND );
        }
    }

    private void writeFlexLibProperties()
        throws MojoExecutionException
    {
        VelocityContext context = new VelocityContext();
        // TODO
        // context.put( "flexClasses", classes );
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
            }
            else
            {
                // resolve files
                ideDependency.setFile( ideDependency.getFile().getAbsoluteFile() );
            }
        }

        Set<IdeDependency> result = new HashSet<IdeDependency>();
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

                    MavenUtils.resolveArtifact( art, resolver, localRepository, remoteRepositories );

                    IdeDependency dep =
                        new IdeDependency( art.getGroupId(), art.getArtifactId(), art.getVersion(),
                                           art.getClassifier(), false, Artifact.SCOPE_TEST.equals( art.getScope() ),
                                           false, false, false, art.getFile(), art.getType(), false, null, 1 );
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

    private void writeAsProperties( String packaging, IdeDependency[] ideDependencies )
        throws MojoExecutionException
    {
        VelocityContext context = new VelocityContext();
        context.put( "dependencies", getDependencies( ideDependencies ) );
        context.put( "locales", getPlainLocales() );
        context.put( "mainSources", getMainSources() );
        context.put( "targetPlayer", targetPlayer );
        context.put( "accessible", accessible );
        context.put( "strict", strict );
        context.put( "useApolloConfig", useApolloConfig( ideDependencies ) );
        context.put( "verifyDigests", verifyDigests );
        context.put( "showWarnings", showWarnings );

        if ( SWF.equals( packaging ) )
        {
            File sourceFile = MavenUtils.resolveSourceFile( project, this.sourceFile );
            context.put( "mainApplication", sourceFile.getName() );
            context.put( "generateHtmlWrapper", generateHtmlWrapper );
        }
        else if ( SWC.equals( packaging ) )
        {
            context.put( "mainApplication", project.getArtifactId() + ".as" );
            context.put( "includes", "-include-sources " + getPlainSources() );
            context.put( "generateHtmlWrapper", false );
        }
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

    private String getMainSources()
    {
        String mainSources =
            PathUtil.getRelativePath( project.getBasedir(), new File( project.getBuild().getSourceDirectory() ) );
        return mainSources;
    }

    private String getPlainSources()
    {
        if ( includeSources == null )
        {
            return plain( getSourceRoots() );
        }

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
