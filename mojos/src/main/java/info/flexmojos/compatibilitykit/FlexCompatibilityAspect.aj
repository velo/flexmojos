/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.compatibilitykit;

import static info.flexmojos.compatibilitykit.VersionUtils.isMaxVersionOK;
import static info.flexmojos.compatibilitykit.VersionUtils.isMinVersionOK;
import static info.flexmojos.compatibilitykit.VersionUtils.splitVersion;

import org.aspectj.lang.reflect.MethodSignature;

public aspect FlexCompatibilityAspect
{

    pointcut compatibilityMethods() : call(@FlexCompatibility * *(*)) 
                            || call(@FlexCompatibility * *());

    void around() : compatibilityMethods() {
        FlexMojo mojo = (FlexMojo) thisJoinPoint.getTarget();
        MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
        int[] fdkVersion = splitVersion( mojo.getFDKVersion() );
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
                                     + " Max version: " + maxVersion + " Current version: " + mojo.getFDKVersion() );
        }
    }
}
