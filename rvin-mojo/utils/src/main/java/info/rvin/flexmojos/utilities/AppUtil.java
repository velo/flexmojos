package info.rvin.flexmojos.utilities;

import java.io.File;
import java.io.FileFilter;

import org.apache.maven.project.MavenProject;

public class AppUtil {
	
	private AppUtil(){}
	
	/**
	 * @param project maven project
	 * @param sourceFile sugested name on pom
	 * @return
	 */
	public static File resolveSourceFile(MavenProject project,
			String sourceFile) {

		File sourceDirectory = new File(project.getBuild().getSourceDirectory());

		if (sourceFile != null) {
			return new File(sourceDirectory, sourceFile);
		} else {
			File[] files = sourceDirectory.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			});

			if (files.length == 1) {
				return files[0];
			}
			if (files.length > 1) {
				for (File file : files) {
					if (file.getName().equalsIgnoreCase("main.mxml")
							|| file.getName().equalsIgnoreCase("main.as")) {
						return file;
					}
				}
			}
			return null;
		}
	}

}
