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
package org.sonatype.flexmojos.generator.iface;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.generator.api.GenerationException;
import org.sonatype.flexmojos.generator.api.GenerationRequest;
import org.sonatype.flexmojos.generator.api.Generator;
import org.sonatype.flexmojos.generator.iface.model.Definition;
import org.sonatype.flexmojos.generator.iface.model.Excludes;
import org.sonatype.flexmojos.generator.iface.model.ForceArrays;
import org.sonatype.flexmojos.generator.iface.model.MethodSignature;

import com.thoughtworks.xstream.XStream;

import flex2.tools.oem.Configuration;

@SuppressWarnings( "restriction" )
public class IFaceGeneratorTest
    extends PlexusTestCase
{

    private Generator generator;

    private File files;

    private XStream xstream;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.generator = lookup( Generator.class, "internal-ifaces" );

        files = new File( "./target/files" );
        FileUtils.forceDelete( files );

        xstream = new XStream();
        xstream.processAnnotations( Excludes.class );
        xstream.processAnnotations( ForceArrays.class );
        xstream.processAnnotations( Definition.class );
        xstream.processAnnotations( MethodSignature.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        this.generator = null;

        super.tearDown();
    }

    public void testGenerate()
        throws GenerationException, IOException
    {
        GenerationRequest request = new GenerationRequest();
        request.addExtraOption( Excludes.NAME, getExcludes() );
        request.addExtraOption( "forceArrays", getArrays() );
        request.setTransientOutputFolder( files );
        request.addClass( Configuration.class.getName(), null );
        // request.addClass( Builder.class.getName(), null );
        // request.addClass( Library.class.getName(), null );
        // request.addClass( Application.class.getName(), null );
        request.setClassLoader( Thread.currentThread().getContextClassLoader() );

        generator.generate( request );

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager( null, null, null );

        Iterable<? extends JavaFileObject> compilationUnits1 =
            fileManager.getJavaFileObjects( new File( files, "org/sonatype/flexmojos/compiler" ).listFiles() );
        assertTrue( "Problems compiling files", compiler.getTask( null, fileManager, null, null, null,
                                                                  compilationUnits1 ).call() );

        fileManager.close();
    }

    private String getExcludes()
    {
        return xstream.toXML( new Excludes( //
                                            new Definition( Configuration.class,// 
                                                            // new Method( "getLocale", Locale.class
                                                            // ),//
                                                            new MethodSignature( "setLocale", Locale.class )// 
                                            ) ) );
    }

    private String getArrays()
    {
        return xstream.toXML( new ForceArrays( //
                                               new Definition( Configuration.class,// 
                                                               // new Method( "getLocale", Locale.class
                                                               // ),//
                                                               new MethodSignature( "checkActionScriptWarning",
                                                                                    int.class, boolean.class ),// 
                                                               new MethodSignature( "addDefineDirective", String.class,
                                                                                    String.class ),// 
                                                               new MethodSignature( "setFontLanguageRange",
                                                                                    String.class, String.class ),// 
                                                               new MethodSignature( "setComponentManifest",
                                                                                    String.class, File.class ),// 
                                                               new MethodSignature( "setFrameLabel", String.class,
                                                                                    String[].class ),// 
                                                               new MethodSignature( "setLicense", String.class,
                                                                                    String.class ),// 
                                                               new MethodSignature( "setSWFMetaData", int.class,
                                                                                    Object.class ),// 
                                                               new MethodSignature( "setToken", String.class,
                                                                                    String.class ),// 
                                                               new MethodSignature( "addRuntimeSharedLibraryPath",
                                                                                    String.class, String[].class,
                                                                                    String[].class ),// 
                                                               new MethodSignature( "addExtensionLibraries",
                                                                                    File.class, List.class )// 
                                               ) ) );
    }
}
