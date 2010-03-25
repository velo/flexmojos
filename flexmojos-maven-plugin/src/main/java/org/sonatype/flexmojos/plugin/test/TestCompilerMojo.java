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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.scope;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.CACHING;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.COMPILE;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.EXTERNAL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.INTERNAL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.MERGED;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.RSL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.TEST;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;
import org.sonatype.flexmojos.compiler.MxmlcConfigurationHolder;
import org.sonatype.flexmojos.plugin.compiler.MxmlcMojo;
import org.sonatype.flexmojos.plugin.utilities.CollectionUtils;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.test.util.PathUtil;

/**
 * <p>
 * Goal which compiles the Flex test into a runnable application
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal test-compile
 * @requiresDependencyResolution compile
 * @phase test-compile
 * @configurator flexmojos
 */
public class TestCompilerMojo
    extends MxmlcMojo
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

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !PathUtil.exist( testCompileSourceRoots ) )
        {
            getLog().warn( "Skipping compiler, test source path doesn't exist." );
            return;
        }

        executeCompiler( new MxmlcConfigurationHolder( this, getSourceFile() ), true );
    }

    @Override
    public File[] getSourcePath()
    {
        return PathUtil.getFiles( testCompileSourceRoots );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getIncludeLibraries()
    {
        return MavenUtils.getFiles( getDependencies( type( SWC ),// 
                                                     anyOf( scope( INTERNAL ), scope( RSL ), scope( CACHING ),
                                                            scope( TEST ) ),//
                                                     not( GLOBAL_MATCHER ) ) );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getExternalLibraryPath()
    {
        return MavenUtils.getFiles( getGlobalArtifact() );
    }

    @Override
    public String[] getRuntimeSharedLibraries()
    {
        return null;
    }

    @Override
    public IRuntimeSharedLibraryPath[] getRuntimeSharedLibraryPath()
    {
        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getLibraryPath()
    {
        return MavenUtils.getFiles( getDependencies( type( SWC ),//
                                                     anyOf( scope( MERGED ), scope( EXTERNAL ), scope( COMPILE ),
                                                            scope( null ) ),//
                                                     not( GLOBAL_MATCHER ) ),//
                                    getCompiledResouceBundles() );
    }

    @Override
    public String[] getLocale()
    {
        return CollectionUtils.merge( runtimeLocales, super.getLocale() ).toArray( new String[0] );
    }

    @Override
    public boolean isUpdateSecuritySandbox()
    {
        // not optional for tests, flexmojos needs sandbox security disabled
        return true;
    }
}
