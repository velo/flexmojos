package org.sonatype.flexmojos.plugin.compiler;

import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.PatternSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.plugin.compiler.attributes.converter.RuledClasses;
import org.sonatype.flexmojos.util.PathUtil;

/**
 * <p>
 * Goal which compiles the Flex sources into a library for either Flex or AIR depending.
 * </p>
 * <p>
 * The Flex Compiler plugin compiles all ActionScript sources. It can compile the source into 'swc' files. The plugin
 * supports the 'swc' packaging.
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal compile-swc
 * @requiresDependencyResolution compile
 * @phase compile
 * @configurator flexmojos
 */
public class CompcMojo
    extends AbstractMavenFlexCompilerConfiguration<ICompcConfiguration, CompcMojo>
    implements ICompcConfiguration, Mojo
{

    /**
     * Writes a digest to the catalog.xml of a library. This is required when the library will be used as runtime shared
     * libraries
     * <p>
     * Equivalent to -compute-digest
     * </p>
     * 
     * @parameter expression="${flex.computeDigest}"
     */
    protected Boolean computeDigest;

    /**
     * Output the library as an open directory instead of a SWC file
     * <p>
     * Equivalent to -directory
     * </p>
     * 
     * @parameter expression="${flex.directory}"
     */
    private Boolean directory;

    /**
     * Automatically include all declared namespaces
     * 
     * @parameter default-value="false" expression="${flex.includeAllNamespaces}"
     */
    private boolean includeAllNamespaces;

    /**
     * Inclusion/exclusion patterns used to filter classes to include in the output SWC
     * <p>
     * Equivalent to -include-classes
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeClasses&gt;
     *   &lt;class&gt;org.sonatype.flexmojos.MyClass&lt;/class&gt;
     *   &lt;class&gt;org.sonatype.flexmojos.YourClass&lt;/class&gt;
     *   &lt;classSet&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;com/mycompany/*&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;com/mycompany/ui/*&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/classSet&gt;
     *   &lt;classSet&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;com/mycompany/*&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;com/mycompany/ui/*&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/classSet&gt;
     * &lt;/includeClasses&gt;
     * </pre>
     * 
     * @parameter
     */
    private RuledClasses includeClasses;

    /**
     * Inclusion/exclusion patterns used to filter resources to be include in the output SWC
     * <p>
     * Equivalent to -include-file
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeFiles&gt;
     *   &lt;includeFile&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;*.xml&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;excluded-*.xml&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/includeFile&gt;
     * &lt;/includeFiles&gt;
     * </pre>
     * 
     * @parameter
     */
    private PatternSet[] includeFiles;

    /**
     * If true, manifest entries with lookupOnly=true are included in SWC catalog
     * <p>
     * Equivalent to -include-lookup-only
     * </p>
     * 
     * @parameter expression="${flex.includeLookupOnly}"
     */
    private Boolean includeLookupOnly;

    /**
     * All classes in the listed namespaces are included in the output SWC
     * <p>
     * Equivalent to -include-namespaces
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeNamespaces&gt;
     *   &lt;namespace&gt;http://mynamespace.com&lt;/namespace&gt;
     * &lt;/includeNamespaces&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<String> includeNamespaces;

    /**
     * A list of directories and source files to include in the output SWC
     * <p>
     * Equivalent to -include-sources
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeSources&gt;
     *   &lt;includeSource&gt;${project.build.sourceDirectory}&lt;/includeSource&gt;
     * &lt;/includeSources&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] includeSources;

    /**
     * A list of named stylesheet resources to include in the output SWC
     * <p>
     * Equivalent to -include-stylesheet
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeStylesheets&gt;
     *   &lt;stylesheet&gt;${basedir}/mystyle.css&lt;/stylesheet&gt;
     * &lt;/includeStylesheets&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] includeStylesheets;

    /**
     * DOCME Guess what, undocumented by adobe. Looks like it was overwritten by source paths
     * <p>
     * Equivalent to -root
     * </p>
     * 
     * @parameter expression="${flex.root}"
     * @deprecated
     */
    private String root;

    @Override
    public Result doCompile( ICompcConfiguration cfg, boolean synchronize )
        throws Exception
    {
        return compiler.compileSwc( cfg, synchronize );
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !PathUtil.exist( compileSourceRoots ))
        {
            getLog().warn( "Skipping compiler, source path doesn't exist." );
            return;
        }

        executeCompiler( this, true );

        if ( localesRuntime != null )
        {
            List<Result> results = new ArrayList<Result>();
            for ( String locale : localesRuntime )
            {
                CompcMojo cfg = this.clone();
                configureResourceBundle( locale, cfg );
                results.add( executeCompiler( cfg, fullSynchronization ) );
            }

            wait( results );
        }
    }

    public Boolean getComputeDigest()
    {
        return computeDigest;
    }

    public Boolean getDirectory()
    {
        return directory;
    }

    public List<String> getIncludeClasses()
    {
        if ( includeClasses == null )
        {
            return null;
        }

        List<String> classes = new ArrayList<String>();
        if ( includeClasses.getClasses() != null )
        {
            classes.addAll( Arrays.asList( includeClasses.getClasses() ) );
        }

        if ( includeClasses.getClassSets() != null )
        {
            classes.addAll( filterClasses( includeClasses.getClassSets(), getSourcePath() ) );
        }

        return classes;
    }

    public File[] getIncludeFile()
    {
        PatternSet[] patterns;
        if ( includeFiles == null && includeNamespaces == null && includeSources == null && includeClasses == null )
        {
            PatternSet pattern = new PatternSet();
            pattern.addInclude( "*.*" );
            patterns = new PatternSet[] { pattern };
        }
        else if ( includeFiles == null )
        {
            return null;
        }
        else
        {
            patterns = includeFiles;
        }

        List<File> files = new ArrayList<File>();

        for ( Resource resource : resources )
        {
            File directory = new File( resource.getDirectory() );
            if ( !directory.exists() )
            {
                continue;
            }

            for ( PatternSet pattern : patterns )
            {
                DirectoryScanner scanner = scan( directory, pattern );

                String[] included = scanner.getIncludedFiles();
                for ( String file : included )
                {
                    files.add( new File( directory, file ) );
                }
            }
        }

        return files.toArray( new File[0] );
    }

    public Boolean getIncludeLookupOnly()
    {
        return includeLookupOnly;
    }

    public List<String> getIncludeNamespaces()
    {
        if ( includeNamespaces != null )
        {
            return includeNamespaces;
        }

        if ( includeAllNamespaces )
        {
            return getNamespacesUri();
        }

        return null;
    }

    public List<String> getIncludeResourceBundles()
    {
        return includeResourceBundles;
    }

    public File[] getIncludeSources()
    {
        if ( includeFiles == null && getIncludeNamespaces() == null && includeSources == null && includeClasses == null )
        {
            return getSourcePath();
        }
        return includeSources;
    }

    public File[] getIncludeStylesheet()
    {
        return includeStylesheets;
    }

    @Override
    public final String getProjectType()
    {
        return SWC;
    }

    public String getRoot()
    {
        return root;
    }

}
