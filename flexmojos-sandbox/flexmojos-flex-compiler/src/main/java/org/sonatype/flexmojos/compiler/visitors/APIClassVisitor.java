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
package org.sonatype.flexmojos.compiler.visitors;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;

public class APIClassVisitor
    extends ClassAdapter
    implements Constants
{

    public APIClassVisitor( ClassVisitor cv )
    {
        super( cv );
    }

    @Override
    public CodeVisitor visitMethod( int access, String name, String desc, String[] exceptions, Attribute attrs )
    {
        if ( name.equals( "useConsoleLogger" ) && desc.equals( "(ZZZZ)V" ) )
        {
            CodeVisitor cd = cv.visitMethod( ACC_PUBLIC + ACC_STATIC, "useConsoleLogger", "(ZZZZ)V", null, null );
            cd.visitFieldInsn( GETSTATIC, "org/sonatype/flexmojos/compiler/CompilerThreadLocal", "logger",
                               "Ljava/lang/ThreadLocal;" );
            cd.visitMethodInsn( INVOKEVIRTUAL, "java/lang/ThreadLocal", "get", "()Ljava/lang/Object;" );
            cd.visitTypeInsn( CHECKCAST, "flex2/compiler/Logger" );
            cd.visitMethodInsn( INVOKESTATIC, "flex2/compiler/util/ThreadLocalToolkit", "setLogger",
                                "(Lflex2/compiler/Logger;)V" );
            cd.visitInsn( RETURN );
            cd.visitMaxs( 1, 4 );

            return cd;
        }

        if ( name.equals( "usePathResolver" ) && desc.equals( "(Lflex2/compiler/common/SinglePathResolver;)V" ) )
        {
            CodeVisitor cd = super.visitMethod( access, name, desc, exceptions, attrs );
            cd.visitVarInsn( ALOAD, 1 );
            cd.visitFieldInsn( GETSTATIC, "org/sonatype/flexmojos/compiler/CompilerThreadLocal", "pathResolver",
                               "Ljava/lang/ThreadLocal;" );
            cd.visitMethodInsn( INVOKEVIRTUAL, "java/lang/ThreadLocal", "get", "()Ljava/lang/Object;" );
            cd.visitTypeInsn( CHECKCAST, "flex2/compiler/common/SinglePathResolver" );
            cd.visitMethodInsn( INVOKEVIRTUAL, "flex2/compiler/common/PathResolver", "addSinglePathResolver",
                                "(Lflex2/compiler/common/SinglePathResolver;)V" );
            cd.visitInsn( RETURN );
            cd.visitMaxs( 2, 2 );
            return cd;
        }

        return super.visitMethod( access, name, desc, exceptions, attrs );
    }

}
