package org.sonatype.flexmojos.idesupport;

import static org.sonatype.flexmojos.common.FlexExtension.AIR;
import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.eclipse.EclipseConfigFile;
import org.codehaus.plexus.util.ReflectionUtils;
import org.sonatype.flexmojos.test.util.PathUtil;

public class AbstractIdeMojo
{

    protected static final String M2ECLIPSE_NATURE = "org.maven.ide.eclipse.maven2Nature";

    protected static final String M2ECLIPSE_BUILD_COMMAND = "org.maven.ide.eclipse.maven2Builder";

    /**
     * @parameter default-value="true" expression="${enableM2e}"
     */
    private boolean enableM2e;

    /**
     * Implies enableM2e=true
     * 
     * @parameter default-value="false" expression="${useM2Home}"
     */
    protected boolean useM2Home;

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
     * Define the base path to locate resouce bundle files Accept some special tokens:
     * 
     * <pre>
     * {locale}     - replace by locale name
     * </pre>
     * 
     * @parameter default-value="${basedir}/src/main/locales/{locale}"
     */
    protected String resourceBundlePath;

    public AbstractIdeMojo()
    {
        super();
    }

    @Override
    public boolean setup()
        throws MojoExecutionException
    {
        String packaging = project.getPackaging();
        if ( !( SWF.equals( packaging ) || SWC.equals( packaging ) || AIR.equals( packaging ) ) )
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

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultNatures( String packaging )
    {
        super.fillDefaultNatures( packaging );

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

        if ( enableM2e || useM2Home )
        {
            getBuildcommands().add( M2ECLIPSE_BUILD_COMMAND );
        }
    }

    protected Collection<String> getRelativeSources()
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

    @Override
    protected void setupExtras()
        throws MojoExecutionException
    {

        String packaging = project.getPackaging();

        if ( !SWF.equals( packaging ) && !SWC.equals( packaging ) && !AIR.equals( packaging ) )
        {
            return;
        }

        try
        {
            List<EclipseConfigFile> extraConfigs = getExtraConfigs();
            Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses( "additionalConfig", getClass() );
            field.setAccessible( true );
            EclipseConfigFile[] originalConfig = (EclipseConfigFile[]) field.get( this );
            EclipseConfigFile[] configs = extraConfigs.toArray( new EclipseConfigFile[extraConfigs.size()] );

            configs = (EclipseConfigFile[]) ArrayUtils.addAll( configs, originalConfig );
            field.set( this, configs );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error settings project to UTF-8", e );
        }
    }

    protected List<EclipseConfigFile> getExtraConfigs()
    {
        EclipseConfigFile utfConfig = new EclipseConfigFile();
        utfConfig.setName( ".settings/org.eclipse.core.resources.prefs" );
        utfConfig.setContent( getSettingsContent() );

        List<EclipseConfigFile> configs = new ArrayList<EclipseConfigFile>();
        configs.add( utfConfig );
        return configs;
    }

    @SuppressWarnings( "unchecked" )
    Collection<String> getSourceRoots()
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

    private Collection<String> getAbsolutePaths( File[] sourcePaths )
    {
        Collection<String> paths = new HashSet<String>();
        for ( File file : sourcePaths )
        {
            paths.add( file.getAbsolutePath() );
        }
        return paths;
    }

    protected String getSettingsContent()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( '#' ).append( new Date().toString() ).append( '\n' );
        sb.append( "eclipse.preferences.version=1" ).append( '\n' );
        sb.append( "encoding/<project>=UTF-8" ).append( '\n' );
        return sb.toString();
    }

    protected String plain( Collection<String> strings )
    {
        StringBuilder buf = new StringBuilder();
        for ( String string : strings )
        {
            if ( buf.length() != 0 )
            {
                buf.append( ' ' );
            }
            buf.append( string );
        }
        return buf.toString();
    }

}