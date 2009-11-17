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
import org.objectweb.asm.Label;
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
            MethodVisitor mv = cv.visitMethod( ACC_PUBLIC + ACC_STATIC, "useConsoleLogger", "(ZZZZ)V", null, null );
            mv.visitCode();
            mv.visitFieldInsn( GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;" );
            mv.visitLdcInsn( "using logger" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V" );
            mv.visitFieldInsn( GETSTATIC, "org/sonatype/flexmojos/compiler/CompilerThreadLocal", "logger",
                               "Ljava/lang/ThreadLocal;" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/ThreadLocal", "get", "()Ljava/lang/Object;" );
            mv.visitTypeInsn( CHECKCAST, "flex2/compiler/Logger" );
            mv.visitMethodInsn( INVOKESTATIC, "flex2/compiler/util/ThreadLocalToolkit", "setLogger",
                                "(Lflex2/compiler/Logger;)V" );
            mv.visitInsn( RETURN );
            mv.visitMaxs( 2, 4 );
            mv.visitEnd();

            return mv;
        }

        if ( name.equals( "usePathResolver" ) && desc.equals( "(Lflex2/compiler/common/SinglePathResolver;)V" ) )
        {
            MethodVisitor mv =
                cv.visitMethod( ACC_PUBLIC + ACC_STATIC, "usePathResolver",
                                "(Lflex2/compiler/common/SinglePathResolver;)V", null, null );
            mv.visitCode();
            mv.visitFieldInsn( GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;" );
            mv.visitLdcInsn( "using resolver" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V" );
            mv.visitTypeInsn( NEW, "flex2/compiler/common/PathResolver" );
            mv.visitInsn( DUP );
            mv.visitMethodInsn( INVOKESPECIAL, "flex2/compiler/common/PathResolver", "<init>", "()V" );
            mv.visitVarInsn( ASTORE, 1 );
            mv.visitVarInsn( ALOAD, 0 );
            Label l0 = new Label();
            mv.visitJumpInsn( IFNULL, l0 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitMethodInsn( INVOKEVIRTUAL, "flex2/compiler/common/PathResolver", "addSinglePathResolver",
                                "(Lflex2/compiler/common/SinglePathResolver;)V" );
            mv.visitLabel( l0 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESTATIC, "flex2/compiler/common/LocalFilePathResolver", "getSingleton",
                                "()Lflex2/compiler/common/LocalFilePathResolver;" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "flex2/compiler/common/PathResolver", "addSinglePathResolver",
                                "(Lflex2/compiler/common/SinglePathResolver;)V" );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESTATIC, "flex2/compiler/util/URLPathResolver", "getSingleton",
                                "()Lflex2/compiler/util/URLPathResolver;" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "flex2/compiler/common/PathResolver", "addSinglePathResolver",
                                "(Lflex2/compiler/common/SinglePathResolver;)V" );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESTATIC, "flex2/compiler/util/ThreadLocalToolkit", "setPathResolver",
                                "(Lflex2/compiler/common/PathResolver;)V" );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitFieldInsn( GETSTATIC, "org/sonatype/flexmojos/compiler/CompilerThreadLocal", "pathResolver",
                               "Ljava/lang/ThreadLocal;" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/ThreadLocal", "get", "()Ljava/lang/Object;" );
            mv.visitTypeInsn( CHECKCAST, "flex2/compiler/common/SinglePathResolver" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "flex2/compiler/common/PathResolver", "addSinglePathResolver",
                                "(Lflex2/compiler/common/SinglePathResolver;)V" );
            mv.visitInsn( RETURN );
            mv.visitMaxs( 2, 2 );
            mv.visitEnd();

            return mv;
        }

        return super.visitMethod( access, name, desc, arg3, exceptions );
    }

}
