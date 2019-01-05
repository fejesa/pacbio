package com.foo.pacbio.fastq;

/**
 * Represents a FASTQ record as raw data.
 */
public final class FastqRawRecord {
    
    private final String id;
    
    private final byte[] sequence;
    private final byte[] quality;
    
    private final int offset;
    private final int length;
    
    public FastqRawRecord(String id, byte[] sequence, byte[] quality, int offset, int length) {
        this.id = id;
        this.sequence = sequence;
        this.quality = quality;
        this.offset = offset;
        this.length = length;
    }

	public String getId() {
		return id;
	}

	public byte[] getSequence() {
		return sequence;
	}

	public byte[] getQuality() {
		return quality;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
}