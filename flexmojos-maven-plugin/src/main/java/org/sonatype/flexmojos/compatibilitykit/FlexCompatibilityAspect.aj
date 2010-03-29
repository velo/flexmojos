/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.compatibilitykit;

import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.isMaxVersionOK;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.isMinVersionOK;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.splitVersion;

import org.aspectj.lang.reflect.MethodSignature;

public aspect FlexCompatibilityAspect
{

    pointcut compatibilityMethods() : execution(@FlexCompatibility *  *(*)) 
                            || execution(@FlexCompatibility *  *());

    Object around() : compatibilityMethods() {
        FlexMojo mojo = (FlexMojo) thisJoinPoint.getTarget();
        MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
        int[] fdkVersion = splitVersion( mojo.getCompilerVersion() );
        FlexCompatibility compatibility = signature.getMethod().getAnnotation( FlexCompatibility.class );
        String minVersion = compatibility.minVersion();
        String maxVersion = compatibility.maxVersion();

        if ( isMinVersionOK( fdkVersion, splitVersion( minVersion ) )
            && isMaxVersionOK( fdkVersion, splitVersion( maxVersion ) ) )
        {
            return proceed();
        }
        else
        {
            mojo.getLog().debug(
                                 "Skiping method " + signature.getName() + ".\n" + "Min version: " + minVersion
                                     + " Max version: " + maxVersion + " Current version: " + mojo.getCompilerVersion() );
            return null;
        }
    }
}
