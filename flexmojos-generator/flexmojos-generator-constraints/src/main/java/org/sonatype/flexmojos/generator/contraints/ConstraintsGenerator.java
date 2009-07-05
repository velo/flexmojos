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
package org.sonatype.flexmojos.generator.contraints;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.generator.api.GenerationException;
import org.sonatype.flexmojos.generator.api.GenerationRequest;
import org.sonatype.flexmojos.generator.api.Generator;

@Component( role = Generator.class, hint = "constraints" )
public final class ConstraintsGenerator
    extends AbstractLogEnabled
    implements Generator
{

    public void generate( GenerationRequest request )
        throws GenerationException
    {
        for ( String classname : request.getClasses().keySet() )
        {
            Class<?> clazz;
            try
            {
                clazz = request.getClassLoader().loadClass( classname );
            }
            catch ( ClassNotFoundException e )
            {
                throw new GenerationException( e.getMessage(), e );
            }

            List<Field> fieldsToGenerate = new ArrayList<Field>();

            Field[] fields = clazz.getFields();
            for ( Field field : fields )
            {
                if ( isPublic( field.getModifiers() ) && isStatic( field.getModifiers() )
                    && isFinal( field.getModifiers() ) && isPrimitive( field.getType() ) )
                {
                    fieldsToGenerate.add( field );
                }
            }

            if ( fieldsToGenerate.isEmpty() )
            {
                continue;
            }

            File outDir = request.getTransientOutputFolder();
            outDir = new File( outDir, clazz.getPackage().getName().replace( '.', '/' ) );
            outDir.mkdirs();

            File outFile = new File( outDir, clazz.getSimpleName() + ".as" );

            FileWriter fw;
            try
            {
                fw = new FileWriter( outFile );
                fw.append( "package " ).append( clazz.getPackage().getName() ).append( '{' ).append( '\n' );
                fw.append( "public class " ).append( clazz.getSimpleName() ).append( '{' ).append( '\n' );
                for ( Field field : fieldsToGenerate )
                {
                    fw.append( "public static const " ).append( field.getName() ).append( ':' );
                    fw.append( getAsType( field.getType() ) ).append( '=' );
                    fw.append( toString( field.get( clazz.newInstance() ) ) ).append( ';' ).append( '\n' );
                }
                fw.append( '}' ).append( '\n' );
                fw.append( '}' ).append( '\n' );

                fw.flush();

                fw.close();
            }
            catch ( Exception e )
            {
                throw new GenerationException( "Error generating " + clazz.getName(), e );
            }
        }
    }

    private CharSequence toString( Object object )
    {
        if ( object instanceof String )
        {
            return '"' + object.toString() + '"';
        }

        if ( object instanceof Character )
        {
            return '\'' + unicodeToJava( (Character) object ) + '\'';
        }

        return String.valueOf( object );
    }

    private static String unicodeToJava( char ch )
    {
        switch ( ch )
        {
            case 9:
                return "\\t";
            case 10:
                return "\\n";
            case 12:
                return "\\f";
            case 13:
                return "\\r";
            case 34:
                return "\\\"";
            case 92:
                return "\\\\";
            default:
                break;
        }
        if ( ch < 32 || ch > 126 )
            return "\\u" + getHexCode( ch );
        return String.valueOf( ch );
    }

    private static String getHexCode( char ch )
    {
        return new String( new char[] { leastSignificantHexDigit( ch >>> 12 ), leastSignificantHexDigit( ch >>> 8 ),
            leastSignificantHexDigit( ch >>> 4 ), leastSignificantHexDigit( ch ) } );
    }

    private final static String digits = "0123456789ABCDEF";

    private static char leastSignificantHexDigit( int ch )
    {
        return digits.charAt( ch & 0x0f );
    }

    private String getAsType( Class<?> type )
    {
        if ( type.equals( int.class ) || type.equals( Integer.class ) )
        {
            return "int";
        }
        if ( type.equals( long.class ) || type.equals( Long.class ) )
        {
            return "uint";
        }
        if ( type.equals( boolean.class ) || type.equals( Boolean.class ) )
        {
            return "Boolean";
        }
        if ( type.equals( String.class ) || type.equals( char.class ) || type.equals( Character.class ) )
        {
            return "String";
        }
        return "Number";
    }

    private boolean isPrimitive( Class<?> type )
    {
        return type.isPrimitive() || type.equals( String.class ) || type.equals( Boolean.class )
            || type.equals( Character.class ) || type.equals( Integer.class ) || type.equals( Double.class )
            || type.equals( Long.class ) || type.equals( Float.class );
    }

}
