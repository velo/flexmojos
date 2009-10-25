package org.sonatype.flexmojos.compiler;

import java.io.File;

public interface FlexCompiler
{

    int compileSwf( ICommandLineConfiguration configuration, File sourceFile )
        throws Exception;

    int compileSwc( ICompcConfiguration configuration )
        throws Exception;

    int asdoc( final IASDocConfiguration configuration )
        throws Exception;

    int optimize( final IOptimizerConfiguration configuration )
        throws Exception;

    int digest( final IDigestConfiguration configuration )
        throws Exception;

}
