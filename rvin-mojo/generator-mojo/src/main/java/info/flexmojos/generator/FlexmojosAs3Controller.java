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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.granite.generator.as3.AbstractJavaAs3Controller;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.reflect.JavaEntityBean;
import org.granite.generator.as3.reflect.JavaEnum;
import org.granite.generator.as3.reflect.JavaFieldProperty;
import org.granite.generator.as3.reflect.JavaInterface;
import org.granite.generator.as3.reflect.JavaType;

public class FlexmojosAs3Controller extends AbstractJavaAs3Controller {

	private String uid;

	private File outputDirectory;

	private String[] entityTemplateUris;

	private String[] interfaceTemplateUris;

	private String[] beanTemplateUris;

	private String[] enumTemplateUris;

	private String style;

	public FlexmojosAs3Controller(As3TypeFactory as3TypeFactory) {
		super(as3TypeFactory, false);
	}

	@Override
	public File getSourceDir(JavaType javaType) {
		return outputDirectory;
	}

	@Override
	public boolean isUid(JavaFieldProperty fieldProperty) {
		if (uid == null) {
			return super.isUid(fieldProperty);
		}
		return uid.equals(fieldProperty.getName());
	}

	@Override
	public String[] getTemplateUris(JavaType javaType) {
		if (javaType instanceof JavaEnum)
			return getEnumTemplateUris();
		if (javaType instanceof JavaInterface)
			return getInterfaceTemplateUris();
		if (javaType instanceof JavaEntityBean)
			return getEntityTemplateUris();
		return getBeanTemplateUris();
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public void setOutputDirectory(File outputdir) {
		this.outputDirectory = outputdir;
	}

	public void setEntityTemplateUris(String[] entitytemplate) {
		this.entityTemplateUris = entitytemplate;
	}

	public void setInterfaceTemplateUris(String[] interfacetemplate) {
		this.interfaceTemplateUris = interfacetemplate;
	}

	public void setBeanTemplateUris(String[] beanTemplate) {
		this.beanTemplateUris = beanTemplate;
	}

	public void setEnumTemplateUris(String[] enumtemplate) {
		this.enumTemplateUris = enumtemplate;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String[] getEntityTemplateUris() {
		if (entityTemplateUris == null) {
			if ("flex-mojos".equals(style)) {
				entityTemplateUris = new String[] {};
			} else if ("granite-gas3".equals(style)) {
				entityTemplateUris = arrayCopy(ENTITY_TEMPLATE_URIS);
			}
		}
		return entityTemplateUris;
	}

	public String[] getInterfaceTemplateUris() {
		if (interfaceTemplateUris == null) {
			if ("flex-mojos".equals(style)) {
				interfaceTemplateUris = new String[] {};
			} else if ("granite-gas3".equals(style)) {
				interfaceTemplateUris = arrayCopy(INTERFACE_TEMPLATE_URIS);
			}
		}
		return interfaceTemplateUris;
	}

	public String[] getBeanTemplateUris() {
		if (beanTemplateUris == null) {
			if ("flex-mojos".equals(style)) {
				beanTemplateUris = new String[] {};
			} else if ("granite-gas3".equals(style)) {
				beanTemplateUris = arrayCopy(BEAN_TEMPLATE_URIS);
			}
		}
		return beanTemplateUris;
	}

	public String[] getEnumTemplateUris() {
		if (enumTemplateUris == null) {
			if ("flex-mojos".equals(style)) {
				enumTemplateUris = new String[] {};
			} else if ("granite-gas3".equals(style)) {
				enumTemplateUris = arrayCopy(ENUM_TEMPLATE_URIS);
			}
		}
		return enumTemplateUris;
	}

	@Override
	public List<JavaInterface> getJavaTypeInterfaces(Class<?> clazz) {
		List<JavaInterface> interfazes = new ArrayList<JavaInterface>();
		for (Class<?> interfaze : clazz.getInterfaces()) {
			if (interfaze.getClassLoader() instanceof AntClassLoader)
				interfazes.add((JavaInterface) getJavaType(interfaze));
		}
		return interfazes;
	}

	@Override
	public JavaType getJavaTypeSuperclass(Class<?> clazz) {
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null
				&& superclass.getClassLoader() instanceof AntClassLoader)
			return getJavaType(superclass);
		return null;
	}

	@Override
	public boolean accept(Class<?> clazz) {
		return true;
	}

	@Override
	public File getOutputFile(JavaType javaType, String templateUri) {
		File outputFile = super.getOutputFile(javaType, templateUri);
		File parent = outputFile.getParentFile();
		if(!parent.exists()) {
			parent.mkdirs();
		}
		return outputFile;
	}
}
