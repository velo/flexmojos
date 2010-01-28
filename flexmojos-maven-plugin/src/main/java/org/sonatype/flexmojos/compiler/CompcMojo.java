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
package org.sonatype.flexmojos.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.PatternSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.flexmojos.common.AbstractMavenFlexCompilerConfiguration;

import flex2.compiler.CompilerAPI;

public class CompcMojo
    extends AbstractMavenFlexCompilerConfiguration
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
     *   &lt;includeClasse&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;com/mycompany/*&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;com/mycompany/ui/*&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/includeClasse&gt;
     * &lt;/includeClasses&gt;
     * </pre>
     * 
     * @parameter
     */
    private PatternSet[] includeClasses;

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
     * A list of resource bundles to include in the output SWC
     * <p>
     * Equivalent to -include-resource-bundles
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeResourceBundles&gt;
     *   &lt;rb&gt;SharedResources&lt;/rb&gt;
     *   &lt;rb&gt;Collections&lt;/rb&gt;
     * &lt;/includeResourceBundles&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<String> includeResourceBundles;

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

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            super.getCompiler().compileSwc( this );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
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

        return filterClasses( includeClasses, getSourcePath() );
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

    public String getRoot()
    {
        return root;
    }

}
