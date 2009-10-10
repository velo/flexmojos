package org.sonatype.flexmojos.compiler;

import java.io.File;

public interface FlexCompiler
{

    void compileSwf( ICommandLineConfiguration configuration, File sourceFile );

    void compileSwc( ICompcConfiguration configuration );

}
