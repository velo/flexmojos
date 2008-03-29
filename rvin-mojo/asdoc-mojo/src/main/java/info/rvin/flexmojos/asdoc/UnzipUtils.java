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
			File entryDestination = new File(destination,
					entry.getName());
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
