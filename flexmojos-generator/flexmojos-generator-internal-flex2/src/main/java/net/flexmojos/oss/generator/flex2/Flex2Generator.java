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
package net.flexmojos.oss.generator.flex2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import net.flexmojos.oss.generator.GenerationException;
import net.flexmojos.oss.generator.GenerationRequest;
import net.flexmojos.oss.generator.Generator;

@Component( role = Generator.class, hint = "flex2" )
public class Flex2Generator
    extends AbstractLogEnabled
    implements Generator
{

    private static final String COMPC = "flex2.tools.Compc";

    private static final String COMPILER = "flex2.tools.Compiler";

    public void generate( GenerationRequest request )
        throws GenerationException
    {
        Map<String, File> classes = request.getClasses();
        if ( !classes.containsKey( COMPC ) )
        {
            throw new GenerationException( "Class not found: " + COMPC );
        }
        if ( !classes.containsKey( COMPILER ) )
        {
            throw new GenerationException( "Class not found: " + COMPILER );
        }

        try
        {
            editCompc( classes.get( COMPC ), request.getTransientOutputFolder() );
            editMxmlc( classes.get( COMPILER ), request.getTransientOutputFolder() );
        }
        catch ( Exception e )
        {
            throw new GenerationException( "Failed to generated wrapper", e );
        }
    }

    @SuppressWarnings(  "deprecation"  )
    private void editMxmlc( File mxmlcJar, File destination )
        throws IOException, TargetLostException
    {
        JavaClass jc = parseClass( mxmlcJar, COMPILER );

        ClassGen cg = new ClassGen( jc );
        ConstantPoolGen p = cg.getConstantPool();

        Method[] methods = cg.getMethods();
        for ( Method method : methods )
        {
            if ( method.getName().equals( "main" ) )
            {
                cg.removeMethod( method );

                MethodGen mg = new MethodGen( method, jc.getClassName(), p );
                InstructionList il = mg.getInstructionList();
                for ( Iterator<?> it = il.iterator(); it.hasNext(); )
                {
                    InstructionHandle ih = (InstructionHandle) it.next();
                    Instruction inst = ih.getInstruction();
                    if ( inst instanceof INVOKESTATIC )
                    {
                        INVOKESTATIC ii = (INVOKESTATIC) inst;
                        String methodName = ii.getMethodName( p );
                        String className = ii.getClassName( p );

                        if (  "java.lang.System".equals( className ) && "exit".equals( methodName )  )
                        {
                            try
                            {
                                il.delete( ih );
                            }
                            catch ( TargetLostException e )
                            {
                                InstructionHandle next = (InstructionHandle) it.next();
                                if ( next == null )
                                {
                                    throw e;
                                }

                                InstructionHandle[] targets = e.getTargets();
                                for ( int i = 0; i < targets.length; i++ )
                                {
                                    InstructionTargeter[] targeters = targets[i].getTargeters();

                                    for ( int j = 0; j < targeters.length; j++ )
                                    {
                                        targeters[j].updateTarget( targets[i], next );
                                    }
                                }
                            }
                        }
                    }
                }
                
                cg.addMethod( mg.getMethod() );
            }
        }

        saveClass( destination, cg, COMPILER );
    }

    private static void editCompc( File mxmlcJar, File destination )
        throws IOException
    {
        JavaClass jc = parseClass( mxmlcJar, COMPC );

        ClassGen cg = new ClassGen( jc );

        Method[] methods = cg.getMethods();
        for ( Method method : methods )
        {
            if ( method.getName().equals( "compc" ) )
            {
                method.setAccessFlags( Constants.ACC_PUBLIC | Constants.ACC_STATIC );
            }
        }

        saveClass( destination, cg, COMPC );
    }

    private static void saveClass( File destination, ClassGen cg, String classname )
        throws FileNotFoundException, IOException
    {
        String classfile = classname.replace( '.', '/' ) + ".class";
        FileOutputStream output = null;
        try
        {
            File destFile = new File( destination, classfile );
            destFile.getParentFile().mkdirs();
            output = new FileOutputStream( destFile );
            cg.getJavaClass().dump( output );
            output.flush();
        }
        finally
        {
            IOUtil.close( output );
        }
    }

    private static JavaClass parseClass( File mxmlcJar, String classname )
        throws ZipException, IOException
    {
        String classfile = classname.replace( '.', '/' ) + ".class";
        JavaClass jc;
        ZipFile jar = null;
        InputStream source = null;
        try
        {
            jar = new ZipFile( mxmlcJar );

            ZipEntry entry = jar.getEntry( classfile );
            source = jar.getInputStream( entry );

            ClassParser p = new ClassParser( source, classname );
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
        return jc;
    }

}
