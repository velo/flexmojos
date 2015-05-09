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
package net.flexmojos.oss.plugin.lifecyclemapping;

import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.codehaus.plexus.component.annotations.Component;
import net.flexmojos.oss.plugin.common.FlexExtension;

@Component( role = LifecycleMapping.class, hint = FlexExtension.SWC )
public class SwcLifecycleMapping
    extends AbstractActionScriptLifecycleMapping
    implements LifecycleMapping
{

    public String getCompiler()
    {
        return "net.flexmojos.oss:flexmojos-maven-plugin:compile-swc";
    }

    @Override
    protected String getPackage() {
        return "net.flexmojos.oss:flexmojos-maven-plugin:package";
    }

}
