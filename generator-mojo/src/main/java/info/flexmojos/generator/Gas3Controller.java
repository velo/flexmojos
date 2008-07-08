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
package info.flexmojos.generator;

import java.util.ArrayList;
import java.util.List;

import org.granite.generator.as3.AbstractJavaAs3Controller;
import org.granite.generator.as3.As3Type;
import org.granite.generator.as3.JavaAs3GenerationConfiguration;
import org.granite.generator.as3.PackageTranslator;
import org.granite.generator.as3.reflect.JavaInterface;
import org.granite.generator.as3.reflect.JavaType;

public class Gas3Controller extends AbstractJavaAs3Controller<JavaAs3GenerationConfiguration> {

	/**
	 * @param config
	 */
	public Gas3Controller(JavaAs3GenerationConfiguration config) {
		super(config);
	}

	@Override
	public As3Type getAs3Type(Class<?> clazz) {
		As3Type as3Type = super.getAs3Type(clazz);
		if (config.getTranslators().isEmpty() || clazz.getPackage() == null)
			return as3Type;

		PackageTranslator translator = null;

		String packageName = clazz.getPackage().getName();
		int weight = 0;
		for (PackageTranslator t : config.getTranslators()) {
			int w = t.match(packageName);
			if (w > weight) {
				weight = w;
				translator = t;
			}
		}

		if (translator != null)
			as3Type = new As3Type(translator.translate(packageName), as3Type.getName());

		return as3Type;
	}

	@Override
	public boolean accept(Class<?> clazz) {
		return true;
	}

	@Override
	public List<JavaInterface> getJavaTypeInterfaces(Class<?> clazz) {
		List<JavaInterface> interfazes = new ArrayList<JavaInterface>();
		for (Class<?> interfaze : clazz.getInterfaces()) {
			if (interfaze.getClassLoader() != null)
				interfazes.add((JavaInterface)getJavaType(interfaze));
		}
		return interfazes;
	}

	@Override
	public JavaType getJavaTypeSuperclass(Class<?> clazz) {
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null && superclass.getClassLoader() != null)
			return getJavaType(superclass);
		return null;
	}


}
