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
package org.sonatype.flexmojos.plugin.compiler.lazyload;

import java.util.Map;

import org.aspectj.lang.reflect.MethodSignature;

public aspect LazyLoadAspect
{

    pointcut getters() :
        ( execution(public * org.sonatype.flexmojos.plugin.common.AbstractMavenFlexCompilerConfiguration.get*() )  &&
             !execution( public * org.sonatype.flexmojos.common.AbstractMavenFlexCompilerConfiguration.getLog() ) &&
             !execution( public * org.sonatype.flexmojos.common.AbstractMavenFlexCompilerConfiguration.getCache() )  ) ||
        execution(public * org.sonatype.flexmojos.compiler.CompcMojo.get*()) ||
        execution(public * org.sonatype.flexmojos.compiler.MxmlcMojo.get*()) ||
        execution(public * org.sonatype.flexmojos.compiler.AsdocMojo.get*());

    Object around() : getters() {
        Map<String, Object> cachedValues = ( (Cacheable) thisJoinPoint.getTarget() ).getCache();

        MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
        String name = signature.getName();

        if ( !cachedValues.containsKey( name ) )
        {
            cachedValues.put( name, proceed() );
        }

        return cachedValues.get( name );
    }

}
