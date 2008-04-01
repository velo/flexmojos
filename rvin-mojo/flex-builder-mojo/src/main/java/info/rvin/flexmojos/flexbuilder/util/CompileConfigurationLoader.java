package info.rvin.flexmojos.flexbuilder.util;

import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * TODO delete when this issue is done
 * http://jira.codehaus.org/browse/MECLIPSE-417
 * 
 * @author velo
 */
public class CompileConfigurationLoader {

	@SuppressWarnings("unchecked")
	public static String getCompilerPluginSetting(MavenProject project,
			String optionName) {
		String value = findCompilerPluginSettingInPlugins(project.getModel()
				.getBuild().getPlugins(), optionName);
		if (value == null
				&& project.getModel().getBuild().getPluginManagement() != null) {
			value = findCompilerPluginSettingInPlugins(project.getModel()
					.getBuild().getPluginManagement().getPlugins(), optionName);
		}
		return value;
	}

	/**
	 * Returns a compiler plugin settings from a list of plugins .
	 * 
	 * @param project
	 *            maven project
	 * @return option value (may be null)
	 */
	@SuppressWarnings("unchecked")
	private static String findCompilerPluginSettingInPlugins(
			List<Plugin> plugins, String optionName) {
		String value = null;

		for (Iterator<Plugin> it = plugins.iterator(); it.hasNext();) {
			Plugin plugin = (Plugin) it.next();

			if (plugin.getArtifactId().equals("flex-compiler-mojo")) {
				Xpp3Dom o = (Xpp3Dom) plugin.getConfiguration();

				// this is the default setting
				if (o != null && o.getChild(optionName) != null) {
					value = o.getChild(optionName).getValue();
				}

				List<PluginExecution> executions = plugin.getExecutions();

				// a different source/target version can be configured for test
				// sources compilation
				for (Iterator<PluginExecution> iter = executions.iterator(); iter
						.hasNext();) {
					PluginExecution execution = (PluginExecution) iter.next();
					o = (Xpp3Dom) execution.getConfiguration();

					if (o != null && o.getChild(optionName) != null) {
						value = o.getChild(optionName).getValue();
					}
				}
			}
		}
		return value;
	}
}
