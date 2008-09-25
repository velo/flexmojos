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
