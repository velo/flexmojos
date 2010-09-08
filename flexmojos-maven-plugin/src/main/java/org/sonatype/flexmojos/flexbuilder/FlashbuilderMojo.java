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
package org.sonatype.flexmojos.flexbuilder;

import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.VelocityContext;

/**
 * Extends the standard Flex Builder configuration.
 * 
 * @author Lance Linder (llinder@gmail.com)
 * @since 3.5
 * @extendsPlugin eclipse
 * @extendsGoal eclipse
 * @goal flashbuilder
 * @requiresDependencyResolution
 */
public class FlashbuilderMojo
    extends FlexbuilderMojo
{
    /**
     * IDE Verion. First release of Flashbuilder is version 6.
     * 
     * @parameter default-value=6
     */
    protected Integer ideVersion;

    @Override
    protected String getAsPropertiesTemplate()
    {
        return "/templates/flashbuilder/actionScriptProperties.vm";
    }

    @Override
    protected VelocityContext getAsPropertiesContext( ProjectType type, Collection<FbIdeDependency> dependencies )
        throws MojoExecutionException
    {
        VelocityContext context = super.getAsPropertiesContext( type, dependencies );

        context.put( "ideVersion", ideVersion );

        return context;
    }

    @Override
    protected String getFlexLibPropertiesTemplate()
    {
        return "/templates/flashbuilder/flexLibProperties.vm";
    }

    @Override
    protected String getFlexPropertiesTemplate()
    {
        return "/templates/flashbuilder/flexProperties.vm";
    }

    /**
     * Utility function to sense flex builder SDK value from framework dependencies. For example: 4.0.0 will import into
     * Flexbuilder as "Flex 4.0" 3.5.0 will import as Flex "3.5". This override differs from the base function in that
     * versions such as 4.0.0 will return 4.0 instead of 4 as in the base implementation.
     * 
     * @return
     */
    @Override
    protected String getFlexSdkVersion()
    {
        String value = super.getFlexSdkVersion();

        if ( !value.equals( "default" ) && !value.contains( "." ) )
            value += ".0";

        return value;
    }

}
