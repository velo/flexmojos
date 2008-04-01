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
