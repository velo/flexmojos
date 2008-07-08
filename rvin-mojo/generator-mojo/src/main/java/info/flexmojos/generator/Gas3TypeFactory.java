/**
 *
 */
package info.flexmojos.generator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.granite.generator.as3.As3Type;
import org.granite.generator.as3.DefaultAs3TypeFactory;

public class Gas3TypeFactory extends DefaultAs3TypeFactory {

	private String[] typeMappings;
	private Log log;
	private final Pattern PATTERN = Pattern
			.compile("\\s*([^\\s]+)\\s+([^\\s]+)+\\s*");

	public Gas3TypeFactory(ClassLoader loader, String[] typeMappings,
			Log log) throws MojoExecutionException {
		super();
		this.typeMappings = typeMappings;
		this.log = log;
		populateCacheWithMappings(loader);
	}

	private void populateCacheWithMappings(final ClassLoader loader) {
		if (this.typeMappings == null)
			return;
		for (String typeMapping : this.typeMappings) {
			Matcher matcher = PATTERN.matcher(typeMapping);
			if (matcher.matches()) {
				final Class<?> javaType = getJavaType(loader, matcher.group(1));
				final As3Type as3Type = newAs3Type(matcher.group(2));
				if (as3Type != null && javaType != null) {
					putInCache(javaType, as3Type);
				}
			}
		}
	}

	private Class<?> getJavaType(final ClassLoader loader, final String typeName) {
		Class<?> javaType = null;
		try {
			javaType = loader.loadClass(typeName);
		} catch (ClassNotFoundException e) { /* ignore */
		}
		if (javaType == null) {
			try {
				javaType = getClass().getClassLoader().loadClass(typeName);
			} catch (ClassNotFoundException e) { /* ignore */
			}
		}
		if (javaType == null) {
			this.log.warn("Cannot load Java class for name '" + typeName
					+ "' This mapping will be ignored.");
		}
		return javaType;
	}

	private As3Type newAs3Type(final String typeName) {
		int i = typeName.lastIndexOf('.');
		if (i > 0) {
			String packageName = typeName.substring(0, i);
			if (packageName.trim().length() > 0) {
				String simpleName = typeName.substring(i + 1);
				if (simpleName.trim().length() > 0) {
					return new As3Type(packageName, simpleName);
				}
			}
		} else {
			return new As3Type("", typeName);
		}
		this.log
				.warn("Invalid AS3 type name '"
						+ typeName
						+ "'. Type name needs to be a fully qualified AS3 class name. This mapping will be ignored.");
		return null;
	}
}
