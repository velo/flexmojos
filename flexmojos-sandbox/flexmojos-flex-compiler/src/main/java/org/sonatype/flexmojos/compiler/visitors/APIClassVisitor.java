/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
