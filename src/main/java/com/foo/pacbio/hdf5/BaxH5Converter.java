package com.foo.pacbio.hdf5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.foo.pacbio.fastq.FastqRawRecord;
import com.foo.pacbio.fastq.FastqWriter;
import com.foo.pacbio.fastq.SimpleFastqWriter;

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

	/**
	 * Converts PacBio HDF5 files to FASTQ format. Input file must contain the
	 * multiparts of the sequencer output. Output is automatically written out is
	 * GZIP format.
	 * 
	 * @param input  The base file (bas.h5).
	 * @param output The FASTQ file.
	 * @throws IOException If conversion is failed.
	 */
	public void multiPart(final Path input, final Path output) throws Exception {
		List<Path> files;
		IHDF5SimpleReader reader = HDF5Factory.openForReading(input.toFile());
		try {
			final String[] parts = reader.readStringArray(BaxH5Converter.MULTIPART_PATH);
			files = Arrays.stream(parts).map(input::resolveSibling).collect(Collectors.toList());
			// Resolve multiparts and check the existence of them
			for (final Path file : files) {
				if (!Files.exists(file)) {
					throw new IOException("File does not exist: " + file.toString());
				}
			}
		} finally {
			reader.close();
		}

		for (Path file : files) {
			convert(file, output, true);
		}
	}

	/**
	 * Converts single part PacBio HDF5 file to FASTQ format.
	 * 
	 * @param input  The base file (bax.h5).
	 * @param output The FASTQ file.
	 * @throws Exception If conversion is failed.
	 */
	public void singlePart(Path input, Path output, boolean append) throws Exception {
		convert(input, output, append);
	}

	private void convert(Path input, Path output, boolean append) throws Exception {

		try (BaxH5Reader reader = new BaxH5Reader(input);
				FastqWriter fw = new SimpleFastqWriter(output);) {

			for (Iterator<FastqRawRecord> iterator = reader.iterator(); iterator.hasNext();) {
				fw.write(iterator.next());
			}
		}
	}
}