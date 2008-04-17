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
package info.rvin.flexmojos.asdoc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipUtils {

	public static void unzip(InputStream input, File destination)
			throws IOException {
		final int BUFFER = 2048;

		BufferedInputStream bis = new BufferedInputStream(input, BUFFER);
		ZipInputStream zis = new ZipInputStream(bis);
		ZipEntry entry = null;
		while ((entry = zis.getNextEntry()) != null) {
			File entryDestination = new File(destination, entry.getName());
			if (entry.isDirectory()) {
				entryDestination.mkdirs();
				continue;
			} else {
				entryDestination.createNewFile();
			}

			int bytesLidos = 0;
			byte dados[] = new byte[BUFFER];

			FileOutputStream fos = new FileOutputStream(entryDestination);
			BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
			while ((bytesLidos = zis.read(dados, 0, BUFFER)) != -1) {
				dest.write(dados, 0, bytesLidos);
			}
			dest.flush();
			dest.close();
			fos.close();
		}
		zis.close();
		bis.close();
	}

}
