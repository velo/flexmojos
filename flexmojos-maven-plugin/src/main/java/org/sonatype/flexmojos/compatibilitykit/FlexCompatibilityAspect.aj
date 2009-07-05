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
package org.sonatype.flexmojos.compatibilitykit;

import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.isMaxVersionOK;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.isMinVersionOK;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.splitVersion;

import org.aspectj.lang.reflect.MethodSignature;

public aspect FlexCompatibilityAspect
{

    pointcut compatibilityMethods() : call(@FlexCompatibility * *(*)) 
                            || call(@FlexCompatibility * *());

    void around() : compatibilityMethods() {
        FlexMojo mojo = (FlexMojo) thisJoinPoint.getTarget();
        MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
        int[] fdkVersion = splitVersion( mojo.getCompilerVersion() );
        FlexCompatibility compatibility = signature.getMethod().getAnnotation( FlexCompatibility.class );
        String minVersion = compatibility.minVersion();
        String maxVersion = compatibility.maxVersion();

        if ( isMinVersionOK( fdkVersion, splitVersion( minVersion ) )
            && isMaxVersionOK( fdkVersion, splitVersion( maxVersion ) ) )
        {
            proceed();
        }
        else
        {
            mojo.getLog().debug(
                                 "Skiping method " + signature.getName() + ".\n" + "Min version: " + minVersion
                                     + " Max version: " + maxVersion + " Current version: " + mojo.getCompilerVersion() );
        }
    }
}
