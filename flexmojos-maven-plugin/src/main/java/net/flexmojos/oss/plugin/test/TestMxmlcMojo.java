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
package net.flexmojos.oss.plugin.test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.anyOf;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.scope;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.type;
import static net.flexmojos.oss.plugin.common.FlexExtension.SWC;
import static net.flexmojos.oss.plugin.common.FlexScopes.INTERNAL;
import static net.flexmojos.oss.plugin.common.FlexScopes.TEST;

import java.io.File;

import net.flexmojos.oss.plugin.compiler.MxmlcMojo;
import net.flexmojos.oss.plugin.compiler.attributes.Module;
import net.flexmojos.oss.plugin.utilities.MavenUtils;

/**
 * <p>
 * Goal which compiles the SWF including all TEST libraries.
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.5
 * @goal test-swf
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class TestMxmlcMojo
    extends MxmlcMojo
{
    @Override
    public String getClassifier()
    {
        return "test";
    }

    @Override
    public Module[] getModules()
    {
        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getIncludeLibraries()
    {
        return MavenUtils.getFiles( getDependencies( type( SWC ), anyOf( scope( INTERNAL ), scope( TEST ) ),
                                                     not( GLOBAL_MATCHER ) ) );
    }

}
