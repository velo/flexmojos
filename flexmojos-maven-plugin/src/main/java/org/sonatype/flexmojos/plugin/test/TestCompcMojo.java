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
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;
import org.sonatype.flexmojos.plugin.compiler.flexbridge.MavenPathResolver;
import org.sonatype.flexmojos.plugin.utilities.CollectionUtils;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
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
 * @since 4.0
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
        return PathUtil.getExistingFiles( testCompileSourceRoots );
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
        return CollectionUtils.merge( super.getLocalesRuntime(), super.getLocale() ).toArray( new String[0] );
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

        files.addAll( PathUtil.getExistingFilesList( testCompileSourceRoots ) );
        files.addAll( Arrays.asList( super.getSourcePath() ) );

        return files.toArray( new File[0] );
    }

}
