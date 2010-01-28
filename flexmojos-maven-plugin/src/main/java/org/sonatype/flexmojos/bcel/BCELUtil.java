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
package org.sonatype.flexmojos.bcel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.compiler.FlexCompiler;

public class BCELUtil
{

    private static final String COMPILER_API = "flex2.compiler.util.ThreadLocalToolkit";

    public static FlexCompiler initializeCompiler( PlexusContainer plexus, File mxmlcJar )
        throws InitializationException
    {
        ClassRealm realm = plexus.createChildRealm( "flex-compiler" );
        try
        {
            realm.loadClass( "org.sonatype.flexmojos.utilities.FlexmojosCompilerAPIHelper" );
            byte[] bytecode = instrumentThreadLocalToolkit( mxmlcJar, realm );
            ClassLoader customClassLoaderWithDoppedAPI = new ByteClassLoader( COMPILER_API, bytecode, realm );
            Class<?> originalClass = customClassLoaderWithDoppedAPI.loadClass( COMPILER_API );
            realm.importFrom( customClassLoaderWithDoppedAPI, COMPILER_API );
            Class<?> importedClass = realm.loadClassFromImport( COMPILER_API );

//            assert importedClass == originalClass;
            Class<?> realmClass = realm.loadClass( COMPILER_API );
//            assert realmClass == originalClass;
        }
        catch ( ClassNotFoundException e )
        {
            throw new InitializationException( "Unable to create a tampered version of 'flex2.compiler.CompilerAPI'", e );
        }
        catch ( MalformedURLException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }

        try
        {
            plexus.setLookupRealm( realm );
            return plexus.lookup( FlexCompiler.class );
        }
        catch ( ComponentLookupException e )
        {
            throw new InitializationException( "Unable to lookup for FlexCompiler", e );
        }
        finally
        {
//            plexus.setLookupRealm( originalRealm );
        }
    }

    private static byte[] instrumentThreadLocalToolkit( File mxmlcJar, ClassRealm realm )
        throws MalformedURLException, ClassNotFoundException, IOException
    {
        JavaClass jc;
        ZipFile jar = null;
        InputStream source = null;
        try
        {
            jar = new ZipFile( mxmlcJar );

            ZipEntry entry = jar.getEntry( COMPILER_API.replace( '.', '/' ) + ".class" );
            source = jar.getInputStream( entry );

            ClassParser p = new ClassParser( source, COMPILER_API );
            jc = p.parse();
        }
        finally
        {
            IOUtil.close( source );
            if ( jar != null )
            {
                jar.close();
            }
        }

        ClassGen cg = new ClassGen( jc );
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory f = new InstructionFactory( cg, cp );

        Method[] methods = cg.getMethods();
        for ( Method method : methods )
        {
            Type[] types = method.getArgumentTypes();
            if ( method.getName().equals( "setLogger" ) && types.length == 1
                && types[0].equals( Type.getType( "Lflex2/compiler/Logger;" ) ) )
            {
                InstructionList il = new InstructionList();
                MethodGen mg =
                    new MethodGen( method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null,
                                   method.getName(), null, il, cp );

                il.append( InstructionFactory.createLoad( Type.OBJECT, 0 ) );
                il.append( f.createInvoke( "org.sonatype.flexmojos.utilities.FlexmojosCompilerAPIHelper",
                                           "fixPathResolver", new ObjectType( "flex2.compiler.common.PathResolver" ),
                                           new Type[] { new ObjectType( "flex2.compiler.common.PathResolver" ) },
                                           Constants.INVOKESTATIC ) );
                il.append( InstructionFactory.createStore( Type.OBJECT, 0 ) );
                il.append( f.createFieldAccess( "bcel.helper.Tampered", "resolver",
                                                new ObjectType( "java.lang.ThreadLocal" ), Constants.GETSTATIC ) );
                il.append( InstructionFactory.createLoad( Type.OBJECT, 0 ) );
                il.append( f.createInvoke( "java.lang.ThreadLocal", "set", Type.VOID, new Type[] { Type.OBJECT },
                                           Constants.INVOKEVIRTUAL ) );
                il.append( InstructionFactory.createReturn( Type.VOID ) );
                mg.setMaxStack();
                mg.setMaxLocals();

                cg.removeMethod( method );
                cg.addMethod( mg.getMethod() );

                il.dispose();
            }
        }

        cg.addField( new FieldGen( Modifier.STATIC, Type.BOOLEAN, "aaisBcelGenerated", cp ).getField() );

        byte[] bytecode = cg.getJavaClass().getBytes();
        return bytecode;
    }

}
