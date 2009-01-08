/**
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.rvin.flexmojos.flexbuilder;

import static info.rvin.flexmojos.utilities.MavenUtils.resolveArtifact;
import info.flexmojos.utilities.PathUtil;
import info.rvin.flexmojos.utilities.CompileConfigurationLoader;
import info.rvin.flexmojos.utilities.MavenUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.eclipse.EclipseConfigFile;
import org.apache.maven.plugin.eclipse.EclipsePlugin;
import org.apache.maven.plugin.ide.IdeDependency;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.velocity.VelocityComponent;

/**
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

    /**
     * @parameter default-value="true" expression="${enableM2e}"
     */
    private boolean enableM2e;

    private static final String SWC = "swc";

    private static final String SWF = "swf";

    private static final String RB_SWC = "rb.swc";

    /**
     * @component
     */
    private VelocityComponent velocityComponent;

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

    @Override
    public void writeConfiguration( IdeDependency[] deps )
        throws MojoExecutionException
    {
        super.writeConfiguration( deps );

        String packaging = project.getPackaging();
        writeAsProperties( packaging );

        if ( SWF.equals( packaging ) )
        {
            writeFlexProperties();
        }
        else if ( SWC.equals( packaging ) )
        {
            writeFlexLibProperties();
        }
        else
        {
            throw new MojoExecutionException( "Unexpected packaging " + packaging );
        }

    }

    private void writeFlexLibProperties()
        throws MojoExecutionException
    {
        VelocityContext context = new VelocityContext();
        // TODO
        // context.put( "flexClasses", classes );
        // context.put( "includeFiles", files );

        runVelocity( "/flexLibProperties.vm", ".flexLibProperties", context );
    }

    private void writeFlexProperties()
        throws MojoExecutionException
    {
        VelocityContext context = new VelocityContext();

        runVelocity( "/flexProperties.vm", ".flexProperties", context );
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

    @SuppressWarnings( "unchecked" )
    protected Set<Artifact> getDependencyArtifacts()
        throws MojoExecutionException
    {
        ArtifactResolutionResult arr;
        try
        {
            arr =
                resolver.resolveTransitively( project.getDependencyArtifacts(), project.getArtifact(),
                                              remoteRepositories, localRepository, artifactMetadataSource );
        }
        catch ( AbstractArtifactResolutionException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        List<Artifact> dependencies = new ArrayList<Artifact>( arr.getArtifacts() );
        List<Artifact> extraRbs = resolveResourceBundles( dependencies );

        Set<Artifact> result = new HashSet<Artifact>();
        result.addAll( dependencies );
        result.addAll( extraRbs );
        return result;
    }

    private List<Artifact> resolveResourceBundles( List<Artifact> dependencies )
        throws MojoExecutionException
    {
        List<Artifact> extraRbs = new ArrayList<Artifact>();
        String[] locales = getLocales();

        for ( Iterator<Artifact> it = dependencies.iterator(); it.hasNext(); )
        {
            Artifact dependency = it.next();
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
                    Artifact resolvedResourceBundle =
                        artifactFactory.createArtifactWithClassifier( dependency.getGroupId(),
                                                                      dependency.getArtifactId(),
                                                                      dependency.getVersion(), dependency.getType(),
                                                                      locale );

                    resolveArtifact( resolvedResourceBundle, resolver, localRepository, remoteRepositories );
                    extraRbs.add( resolvedResourceBundle );
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

    private String[] getLocales()
    {
        String[] locales = new String[0];
        String[] deprecatedLocales = CompileConfigurationLoader.getCompilerPluginSettings( project, "locales" );
        String[] runtimeLocales = CompileConfigurationLoader.getCompilerPluginSettings( project, "runtimeLocales" );
        String[] compiledLocales = CompileConfigurationLoader.getCompilerPluginSettings( project, "compiledLocales" );
        String defaultLocale = CompileConfigurationLoader.getCompilerPluginSetting( project, "defaultLocale" );

        locales = (String[]) ArrayUtils.addAll( locales, deprecatedLocales );
        locales = (String[]) ArrayUtils.addAll( locales, runtimeLocales );
        locales = (String[]) ArrayUtils.addAll( locales, compiledLocales );
        if ( defaultLocale != null )
        {
            locales = (String[]) ArrayUtils.add( locales, defaultLocale );
        }
        return locales;
    }

    private void writeAsProperties( String packaging )
        throws MojoExecutionException
    {
        VelocityContext context = new VelocityContext();
        context.put( "dependencies", getDependencyArtifacts() );
        context.put( "locales", getPlainLocales() );
        context.put( "mainSources", getMainSources() );
        if ( SWF.equals( packaging ) )
        {
            File sourceFile =
                MavenUtils.resolveSourceFile( project,
                                              CompileConfigurationLoader.getCompilerPluginSetting( project,
                                                                                                   "sourceFile" ) );
            context.put( "mainApplication", sourceFile.getName() );
        }
        else if ( SWC.equals( packaging ) )
        {
            context.put( "mainApplication", project.getArtifactId() + ".as" );
            context.put( "includes", "-include-sources " + getPlainSources() );
        }
        context.put( "sources", getRelativeSources() );
        runVelocity( "/actionScriptProperties.vm", ".actionScriptProperties", context );
    }

    private String getMainSources()
    {
        String mainSources =
            PathUtil.getRelativePath( project.getBasedir(), new File( project.getBuild().getSourceDirectory() ) );
        return mainSources;
    }

    private String getPlainSources()
    {
        String[] sources = CompileConfigurationLoader.getCompilerPluginSettings( project, "includeSources" );
        if ( sources == null )
        {
            sources = new String[] { project.getBuild().getSourceDirectory() };
        }
        return plain( sources );
    }

    private List<String> getRelativeSources()
    {
        List<String> sources = new ArrayList<String>();
        List<String> sourceRoots = getSourceRoots();
        for ( String sourceRoot : sourceRoots )
        {
            File source = new File( sourceRoot );
            if ( source.exists() )
            {
                String relative = PathUtil.getRelativePath( project.getBasedir(), source );
                sources.add( relative );
            }
        }
        return sources;
    }

    @SuppressWarnings( "unchecked" )
    private List<String> getSourceRoots()
    {
        List<String> sources = new ArrayList<String>();
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
        return sources;
    }

    private String getPlainLocales()
    {
        String[] locales = getLocales();
        String buf = plain( locales );
        return buf;
    }

    private String plain( String[] locales )
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
