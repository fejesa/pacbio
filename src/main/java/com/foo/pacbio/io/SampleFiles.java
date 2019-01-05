package com.foo.pacbio.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.utils.IOUtils;

public class SampleFiles {

	/**
	 * Evaluates the given file if gzipped format.
	 */
	public static final boolean isCompressed(final Path p) {
		return p.toAbsolutePath().toString().toLowerCase().endsWith(".gz");
	}

	/**
	 * Compress the given file to gzip format.
	 * @param output The file must be compressed.
	 * @throws IOException
	 */
	public void compress(final Path output) throws IOException {
		if (SampleFiles.isCompressed(output)) {

			final Path compressed = Files.createTempFile("pacbio", ".comp");

			try (FileOutputStream fos = new FileOutputStream(compressed.toFile());
					GZIPOutputStream gos = new GZIPOutputStream(fos);
					FileInputStream in = new FileInputStream(output.toFile());) {
				IOUtils.copy(in, gos);
			}

			Files.move(compressed, output, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}