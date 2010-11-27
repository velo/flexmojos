package org.sonatype.flexmojos.plugin.test;

import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;
import org.sonatype.flexmojos.plugin.common.flexbridge.MavenPathResolver;
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.util.CollectionUtils;
import org.sonatype.flexmojos.util.PathUtil;

import flex2.compiler.common.SinglePathResolver;

/**
 * <p>
 * Goal which compiles a SWC of the test classes for the current project.
 * </p>
 * <p>
 * Equivalent to <a href='http://maven.apache.org/plugins/maven-jar-plugin/test-jar-mojo.html'>jar:test-jar</a> goal
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 2.0
 * @goal test-swc
 * @requiresDependencyResolution test
 * @phase package
 * @configurator flexmojos
 * @threadSafe
 */
public class TestCompcMojo
    extends CompcMojo
{

    /**
     * The maven compile source roots
     * <p>
     * Equivalent to -compiler.source-path
     * </p>
     * List of path elements that form the roots of ActionScript class
     * 
     * @parameter expression="${project.testCompileSourceRoots}"
     * @required
     * @readonly
     */
    private List<String> testCompileSourceRoots;

    /**
     * The maven test resources
     * 
     * @parameter expression="${project.build.testResources}"
     * @required
     * @readonly
     */
    protected List<Resource> testResources;

    @Override
    public String getClassifier()
    {
        return "tests";
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getExternalLibraryPath()
    {
        return MavenUtils.getFiles( getDependencies( type( SWC ) ) );
    }

    @Override
    public List<String> getIncludeClasses()
    {
        return null;
    }

    @Override
    public File[] getIncludeLibraries()
    {
        return null;
    }

    @Override
    public List<String> getIncludeNamespaces()
    {
        return null;
    }

    @Override
    public File[] getIncludeSources()
    {
        return PathUtil.existingFiles( testCompileSourceRoots );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getLibraryPath()
    {
        return MavenUtils.getFiles( getCompiledResouceBundles() );
    }

    @Override
    public String[] getLocale()
    {
        return CollectionUtils.merge( super.getLocalesRuntime(), super.getLocale() );
    }

    @Override
    public String[] getLocalesRuntime()
    {
        return null;
    }

    public SinglePathResolver getMavenPathResolver()
    {
        List<Resource> resources = new ArrayList<Resource>();
        resources.addAll( this.testResources );
        resources.addAll( this.resources );
        return new MavenPathResolver( resources );
    }

    @Override
    public IRuntimeSharedLibraryPath[] getRuntimeSharedLibraryPath()
    {
        return null;
    }

    @Override
    public File[] getSourcePath()
    {
        Set<File> files = new LinkedHashSet<File>();

        files.addAll( PathUtil.existingFilesList( testCompileSourceRoots ) );
        files.addAll( Arrays.asList( super.getSourcePath() ) );

        return files.toArray( new File[0] );
    }

}
