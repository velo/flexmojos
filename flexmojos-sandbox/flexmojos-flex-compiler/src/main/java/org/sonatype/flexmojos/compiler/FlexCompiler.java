package org.sonatype.flexmojos.compiler;


public interface FlexCompiler
{

    int compileSwf( MxmlcConfigurationHolder cfgHolder )
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
