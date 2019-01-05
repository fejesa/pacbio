package com.foo.pacbio.hdf5;

/**
 * FASTQ record as raw data.
 */
final class FastqRawRecord {
    
    final String id;
    
    final byte[] sequence;
    final byte[] quality;
    
    final int offset;
    final int length;
    
    FastqRawRecord(String id, byte[] sequence, byte[] quality, int offset, int length) {
        this.id = id;
        this.sequence = sequence;
        this.quality = quality;
        this.offset = offset;
        this.length = length;
    }
}