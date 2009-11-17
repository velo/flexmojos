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
package org.sonatype.flexmojos.compiler;

import java.io.InputStream;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.sonatype.flexmojos.compiler.visitors.APIClassVisitor;

public class CompilerClassLoader
    extends ClassLoader
{

    private static final String compilerApi = "flex2.compiler.CompilerAPI";

    public CompilerClassLoader( ClassLoader cl )
    {
        super( cl );
    }

    public Class<?> loadAPI()
        throws Exception
    {
        ZipFile mxmlc =
            new ZipFile(
                         "C:/Users/Seven/.m2/repository/com/adobe/flex/compiler/mxmlc/4.0.0.10485/mxmlc-4.0.0.10485.jar" );
        InputStream in = mxmlc.getInputStream( mxmlc.getEntry( "flex2/compiler/CompilerAPI.class" ) );

        try
        {
            ClassReader cr = new ClassReader( in );
            ClassWriter cw = new ClassWriter( cr, 0 );
            ClassVisitor cv = new APIClassVisitor( cw );

            cr.accept( cv, ClassReader.EXPAND_FRAMES );
            byte[] bytecode = cw.toByteArray();

            return super.defineClass( compilerApi, bytecode, 0, bytecode.length );
        }
        finally
        {
            in.close();
            mxmlc.close();
        }
    }

}
