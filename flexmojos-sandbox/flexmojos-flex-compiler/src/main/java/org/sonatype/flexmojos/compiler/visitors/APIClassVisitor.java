/*
 * Java bytecode manipulation with ASM toolkit
 * Copyright (c) 2004, Eugene Kuleshov
 *
 * This library is free software; you can redistribute it and/or            
 * modify it under the terms of the GNU Lesser General Public               
 * License as published by the Free Software Foundation; either             
 * version 2.1 of the License, or (at your option) any later version.       
 *                                                                          
 * This library is distributed in the hope that it will be useful,          
 * but WITHOUT ANY WARRANTY; without even the implied warranty of           
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        
 * Lesser General Public License for more details.                          
 *                                                                          
 * You should have received a copy of the GNU Lesser General Public         
 * License along with this library; if not, write to the Free Software      
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.sonatype.flexmojos.compiler.visitors;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class APIClassVisitor
    extends ClassAdapter
    implements Opcodes
{

    public APIClassVisitor( ClassVisitor cv )
    {
        super( cv );
    }

    @Override
    public MethodVisitor visitMethod( int access, String name, String desc, String arg3, String[] exceptions )
    {
        if ( name.equals( "useConsoleLogger" ) && desc.equals( "(ZZZZ)V" ) )
        {
            MethodVisitor cd = cv.visitMethod( ACC_PUBLIC + ACC_STATIC, "useConsoleLogger", "(ZZZZ)V", null, null );
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
            MethodVisitor cd = super.visitMethod( access, name, desc, arg3, exceptions );
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

        return super.visitMethod( access, name, desc, arg3, exceptions );
    }

}
