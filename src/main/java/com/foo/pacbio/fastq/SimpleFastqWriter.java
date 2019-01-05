package com.foo.pacbio.fastq;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Extends {@code BufferedOutputStream} for FASTQ records writing.
 */
public class SimpleFastqWriter extends BufferedOutputStream implements FastqWriter {

	private static final byte LINE_SEPARATOR = (byte) '\n';

	/** The plus sign in the FASTQ record. */
	private static final byte DELIMITER = (byte) 43;

	public SimpleFastqWriter(Path path) throws FileNotFoundException {
		super(new FileOutputStream(path.toFile(), true));
	}

	@Override
	public void write(FastqRawRecord record) throws IOException {
		write(record.getId().getBytes());

		write(LINE_SEPARATOR);

		write(record.getSequence(), record.getOffset(), record.getLength());
		write(LINE_SEPARATOR);

		write(DELIMITER);
		write(LINE_SEPARATOR);

		write(record.getQuality(), record.getOffset(), record.getLength());
		write(LINE_SEPARATOR);

	}
}