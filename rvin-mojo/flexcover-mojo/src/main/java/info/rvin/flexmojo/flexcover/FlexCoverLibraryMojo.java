package info.rvin.flexmojo.flexcover;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import info.rvin.flexmojo.test.TestLibraryCompilerMojo;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which instruments a Flex project with FlexCover metadata
 * 
 * @extendsPlugin flex-compiler-mojo
 * @extendsGoal test-compile
 * @goal instrument-swc
 * @phase process-test-classes
 * @requiresDependencyResolution
 * @requiresProject
 */
public class FlexCoverLibraryMojo
    extends TestLibraryCompilerMojo
{
    /**
     * Location of the file.
     * 
     * @parameter expression="${project.build.directory}/flexcover"
     * @required
     */
    private File outputDirectory;

    @Override
    public void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        // build.setSourceDirectory("src/test/flex");
        build.setTestOutputDirectory( "target/flexcover-classes/" );
        build.setOutputDirectory( "target/flexcover/" );
        // List<File> paths = Arrays.asList(sourcePaths);
        // paths.add(new File("src/main/flex"));
        // sourcePaths = (File[]) paths.toArray();
        // sourceFile = "TestHarness.mxml";
        super.setUp();
    }

}
