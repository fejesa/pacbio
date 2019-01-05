package com.foo.pacbio.hdf5;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;

/**
 * Converts PacBio HDF5 files to FASTQ format.
 */
public class BaxH5Converter {

	/**
	 * BAX parts constant.
	 */
	private static final String MULTIPART_PATH = "MultiPart/Parts";

	private static final byte LINE_SEPARATOR = (byte) '\n';

	/** The plus sign in the FASTQ record. */
	private static final byte DELIMITER = (byte) 43;

	/**
	 * Converts PacBio HDF5 files to FASTQ format. Input file must contain the
	 * multiparts of the sequencer output. Output is automatically written out is
	 * GZIP format.
	 * 
	 * @param input  The base file (bas.h5).
	 * @param output The FASTQ file.
	 * @throws IOException If conversion is failed.
	 */
	public void fromMultiPart(final Path input, final Path output) throws IOException {
		List<Path> files;
		final IHDF5SimpleReader reader = HDF5Factory.openForReading(input.toFile());
		try {
			final String[] parts = reader.readStringArray(BaxH5Converter.MULTIPART_PATH);
			files = Arrays.stream(parts).map(input::resolveSibling).collect(Collectors.toList());
			// Resolve multiparts and check the existence of them
			for (final Path file : files) {
				if (!Files.exists(file)) {
					throw new IOException(String.format("File does not exist: %s", file.toString()));
				}
			}
		} finally {
			reader.close();
		}
		for (Path file : files) {
			this.convert(file, output, true);
		}
	}

	/**
	 * Converts single part PacBio HDF5 file to FASTQ format.
	 * 
	 * @param input  The base file (bax.h5).
	 * @param output The FASTQ file.
	 * @throws IOException If conversion is failed.
	 */

	public void fromSinglePart(final Path input, final Path output, final boolean append) throws IOException {
		this.convert(input, output, append);
	}

	private void convert(final Path input, final Path output, final boolean append) throws IOException {

		try (FileOutputStream fos = new FileOutputStream(output.toFile(), append);
				OutputStream oss = new BufferedOutputStream(fos);
				BaxH5Reader reader = new BaxH5Reader(input)) {

			for (final Iterator<FastqRawRecord> iterator = reader.iterator(); iterator.hasNext();) {
				final FastqRawRecord record = iterator.next();

				oss.write(record.id.getBytes());
				oss.write(BaxH5Converter.LINE_SEPARATOR);

				oss.write(record.sequence, record.offset, record.length);
				oss.write(BaxH5Converter.LINE_SEPARATOR);

				oss.write(BaxH5Converter.DELIMITER);
				oss.write(BaxH5Converter.LINE_SEPARATOR);

				oss.write(record.quality, record.offset, record.length);
				oss.write(BaxH5Converter.LINE_SEPARATOR);

			}
		}
	}
}