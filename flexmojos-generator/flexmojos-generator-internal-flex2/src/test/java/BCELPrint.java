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
import java.io.InputStream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;

import flex2.tools.Compc;

public class BCELPrint
{
    public static void main( String[] args )
        throws Exception
    {
        Class<?> clazz = Compc.class;
        String classname = clazz.getName();
        String classFile = "/" + classname.replace( '.', '/' ) + ".class";
        InputStream in = BCELPrint.class.getResourceAsStream( classFile );

        ClassParser p = new ClassParser( in, "flex2.tool.Compc" );
        JavaClass jc = p.parse();

        BCELifier b = new BCELifier( jc, System.out );
        b.start();
    }
}
