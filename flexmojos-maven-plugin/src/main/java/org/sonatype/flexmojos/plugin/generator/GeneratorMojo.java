/**
 * Copyright 2008 Marvin Herman Froeder
 * Copyright 2009 Edward Yakop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */
package org.sonatype.flexmojos.plugin.generator;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.flexmojos.generator.SimpleGeneratorMojo;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;

/**
 * This goal generate actionscript 3 code based on Java classes. It does uses Granite GAS3.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @author edward.yakop@gmail.com
 * @goal generate
 * @phase generate-sources
 * @extendsPlugin flexmojos-generator-mojo
 * @extendsGoal generate
 * @requiresDependencyResolution test
 * @since 1.0
 */
public class GeneratorMojo
    extends SimpleGeneratorMojo
{

    public void execute()
        throws MojoExecutionException
    {
        getLog().info(
                       "flexmojos " + MavenUtils.getFlexMojosVersion()
                           + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file" );

        super.execute();
    }

}
