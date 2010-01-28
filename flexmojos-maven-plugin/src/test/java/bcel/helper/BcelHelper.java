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
package bcel.helper;

import java.io.IOException;
import java.io.InputStream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;

public class BcelHelper
{
    public static void main( String[] args ) throws Error, Exception
    {
        print( Original.class );
        print( Tampered.class );
    }

    private static void print( Class<?> clazz )
        throws IOException, ClassFormatError
    {
        System.out.println("\n\n ===== Bcelifiering: " + clazz);
        String classname = clazz.getName();
        String classFile = "/" + classname.replace( '.', '/' ) + ".class";
        InputStream source = BcelHelper.class.getResourceAsStream( classFile );

        ClassParser cp = new ClassParser( source, "ajota" );
        JavaClass jc = cp.parse();

        BCELifier b = new BCELifier( jc, System.out );
        b.start();
    }
}
