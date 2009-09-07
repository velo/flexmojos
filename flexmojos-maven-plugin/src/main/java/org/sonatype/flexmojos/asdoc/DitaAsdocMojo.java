/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonatype.flexmojos.asdoc;

import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.isMinVersionOK;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.splitVersion;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which generates documentation from the ActionScript sources in DITA format.
 * 
 * @goal dita-asdoc
 * @requiresDependencyResolution compile
 */
public class DitaAsdocMojo
    extends AsDocMojo
{

    /**
     * The output directory for the generated documentation.
     * 
     * @parameter default-value="${project.build.directory}/dita-asdoc"
     */
    protected File outputDirectory;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !isMinVersionOK( splitVersion( getCompilerVersion() ), splitVersion( "4.0.0.7219" ) ) )
        {
            getLog().warn( "Skipping Dita Asdoc.  Dita Asdoc is only available on Flex4." );
            return;
        }

        super.execute();
    }

    @Override
    protected void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        super.setUp();

        outputDirectory.mkdirs();
    }

    @Override
    protected void addExtraArgs( List<String> args )
    {
        super.addExtraArgs( args );

        args.add( "-output=" + outputDirectory.getAbsolutePath() );

        args.add( "-keep-xml=true" );
        args.add( "-skip-xsl=true" );
    }
}
