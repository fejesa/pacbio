package com.foo.pacbio.hdf5;

/**
 * Defines a region interval.
 */
final class RegionInterval {

	private final int begin;
	private final int end;

	public RegionInterval(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}
}