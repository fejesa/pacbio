package com.foo.pacbio.fastq;

import java.io.IOException;

/**
 * Defines methods for raw FASTQ record writing.
 *
 */
public interface FastqWriter extends AutoCloseable {

	/**
	 * Writes a FASTQ record to the given destination. 
	 * @param record
	 * @throws IOException
	 */
	void write(FastqRawRecord record) throws IOException;
}