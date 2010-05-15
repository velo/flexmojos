package org.sonatype.flexmojos.plugin.test;

import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;
import org.sonatype.flexmojos.plugin.utilities.CollectionUtils;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.util.PathUtil;

/**
 * <p>
 * Goal which compiles a SWC of the test classes for the current project.
 * </p>
 * <p>
 * Equivalent to <a href='http://maven.apache.org/plugins/maven-jar-plugin/test-jar-mojo.html'>jar:test-jar</a> goal
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal test-swc
 * @requiresDependencyResolution test
 * @phase package
 * @configurator flexmojos
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

    @Override
    public File[] getSourcePath()
    {
        Set<File> files = new LinkedHashSet<File>();

        files.addAll( PathUtil.getExistingFilesList( testCompileSourceRoots ) );
        files.addAll( Arrays.asList( super.getSourcePath() ) );

        return files.toArray( new File[0] );
    }

    @Override
    public List<String> getIncludeClasses()
    {
        return null;
    }

    @Override
    public File[] getIncludeSources()
    {
        return PathUtil.getExistingFiles( testCompileSourceRoots );
    }

    @Override
    public List<String> getIncludeNamespaces()
    {
        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getExternalLibraryPath()
    {
        return MavenUtils.getFiles( getDependencies( type( SWC ) ) );
    }

    @Override
    public File[] getIncludeLibraries()
    {
        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getLibraryPath()
    {
        return MavenUtils.getFiles( getCompiledResouceBundles() );
    }

    @Override
    public IRuntimeSharedLibraryPath[] getRuntimeSharedLibraryPath()
    {
        return null;
    }

    @Override
    public String getClassifier()
    {
        return "tests";
    }

    @Override
    public String[] getLocalesRuntime()
    {
        return null;
    }

    @Override
    public String[] getLocale()
    {
        return CollectionUtils.merge( super.getLocalesRuntime(), super.getLocale() ).toArray( new String[0] );
    }

}
