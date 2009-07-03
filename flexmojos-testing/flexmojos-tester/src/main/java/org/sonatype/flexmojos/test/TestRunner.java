package org.sonatype.flexmojos.test;

import java.io.File;
import java.util.List;

import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;

public interface TestRunner
{
    List<String> run( File swf )
        throws TestRunnerException, LaunchFlashPlayerException;
}
