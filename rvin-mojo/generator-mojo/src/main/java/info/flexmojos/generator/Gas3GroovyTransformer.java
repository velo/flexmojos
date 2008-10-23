/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.generator;

import java.util.ArrayList;
import java.util.List;

import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.as3.As3Type;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.PackageTranslator;
import org.granite.generator.as3.reflect.JavaInterface;
import org.granite.generator.as3.reflect.JavaType;

public class Gas3GroovyTransformer
    extends JavaAs3GroovyTransformer
{
    public Gas3GroovyTransformer( JavaAs3GroovyConfiguration config, Listener listener )
    {
        super( config, listener );
    }

    @Override
    public boolean accept( Input<?> input )
    {
        return true;
    }

    @Override
    public As3Type getAs3Type( Class<?> clazz )
    {
        As3Type as3Type = super.getAs3Type( clazz );
        if ( getConfig().getTranslators().isEmpty() || clazz.getPackage() == null )
            return as3Type;

        PackageTranslator translator = null;

        String packageName = clazz.getPackage().getName();
        int weight = 0;
        for ( PackageTranslator t : getConfig().getTranslators() )
        {
            int w = t.match( packageName );
            if ( w > weight )
            {
                weight = w;
                translator = t;
            }
        }

        if ( translator != null )
            as3Type = new As3Type( translator.translate( packageName ), as3Type.getName() );

        return as3Type;
    }

    @Override
    public JavaType getJavaTypeSuperclass( Class<?> clazz )
    {
        Class<?> superclass = clazz.getSuperclass();
        if ( superclass != null && superclass.getClassLoader() != null )
            return getJavaType( superclass );
        return null;
    }

    @Override
    public List<JavaInterface> getJavaTypeInterfaces( Class<?> clazz )
    {
        List<JavaInterface> interfazes = new ArrayList<JavaInterface>();
        for ( Class<?> interfaze : clazz.getInterfaces() )
        {
            if ( interfaze.getClassLoader() != null )
                interfazes.add( (JavaInterface) getJavaType( interfaze ) );
        }
        return interfazes;
    }

}
