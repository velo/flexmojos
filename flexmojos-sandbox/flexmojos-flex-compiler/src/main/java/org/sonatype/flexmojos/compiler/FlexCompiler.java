package org.sonatype.flexmojos.compiler;

import org.sonatype.flexmojos.compiler.command.Result;

public interface FlexCompiler
{

    Result compileSwf( MxmlcConfigurationHolder cfgHolder, boolean sychronize )
        throws Exception;

    Result compileSwc( ICompcConfiguration configuration, boolean sychronize )
        throws Exception;

    Result asdoc( final IASDocConfiguration configuration, boolean sychronize )
        throws Exception;

    Result optimize( final IOptimizerConfiguration configuration, boolean sychronize )
        throws Exception;

    Result digest( final IDigestConfiguration configuration, boolean sychronize )
        throws Exception;

}
