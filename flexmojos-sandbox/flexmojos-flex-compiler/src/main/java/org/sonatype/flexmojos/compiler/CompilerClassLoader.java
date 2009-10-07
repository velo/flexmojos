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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.sonatype.flexmojos.compiler.visitors.APIClassVisitor;

public class CompilerClassLoader
    extends ClassLoader
{

    public CompilerClassLoader( ClassLoader cl )
    {
        super( cl );
    }

    public Class<?> loadAPI()
        throws ClassNotFoundException
    {
        final String className = "flex2.compiler.API";
        try
        {
            ClassWriter cw = new ClassWriter( true, true );
            ClassVisitor ncv = new APIClassVisitor( cw );

            ClassReader cr = new ClassReader( className );
            cr.accept( ncv, false );
            byte[] bytecode = cw.toByteArray();

            return super.defineClass( className, bytecode, 0, bytecode.length );
        }
        catch ( Exception ex )
        {
            throw new ClassNotFoundException( "Load error: " + ex.toString(), ex );
        }
    }

}
