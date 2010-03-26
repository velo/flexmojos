package org.sonatype.flexmojos.test;

import java.util.List;

import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;

public interface TestRunner
{
    List<String> run( TestRequest testRequest )
        throws TestRunnerException, LaunchFlashPlayerException;
}
