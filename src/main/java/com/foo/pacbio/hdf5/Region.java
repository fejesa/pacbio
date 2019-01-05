package com.foo.pacbio.hdf5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a sequence region boundaries and scoring value. PacBio can
 * generate different kind of region data therefore we store the status of it
 * for filtering; i.e. for FASTQ conversion.
 */
final class Region {

	/** List of region boundaries. */
	private final List<RegionInterval> boundaries;

	private final byte holeStatus;
	private final int regionScore;

	public Region(int[] data, int begin, int end, byte holeStatus) {

		this.holeStatus = holeStatus;

		int hqStart = Integer.MAX_VALUE;
		int hqEnd = -1;

		List<RegionInterval> intervals = new ArrayList<>();

		int score = -1;
		for (int shift = begin; shift < end; shift += 5) {
			int locStart = data[shift + 2];
			int locEnd = data[shift + 3];

			if (data[shift + 1] == 1) { // 'Insert data'
				intervals.add(new RegionInterval(locStart, locEnd));

			} else if (data[shift + 1] == 2) { // 'HQRegion'
				hqStart = Math.min(locStart, hqStart);
				hqEnd = Math.max(locEnd, hqEnd);
				score = Math.max(score, data[shift + 4]);
			}
		}
		
		boundaries = createBoundaries(intervals, hqStart, hqEnd);
		regionScore = score;
	}

	private static List<RegionInterval> createBoundaries(List<RegionInterval> intervals, int hqStart, int hqEnd) {
		return intervals
				.stream()
				.map(i -> from(i, hqStart, hqEnd))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	private static Optional<RegionInterval> from(RegionInterval interval, int hqStart, int hqEnd) {
		int b = Integer.max(interval.getBegin(), hqStart);
		int e = Integer.min(interval.getEnd(), hqEnd);
		return e > b ? Optional.of(new RegionInterval(b, e)) : Optional.empty();
	}
	
	public List<RegionInterval> getBoundaries() {
		return boundaries;
	}

	/**
	 * Indicates how to decode the HoleStatus field. Only ZMWs with a HoleStatus ==
	 * 0 can generate a sequence.
	 * 
	 * @return <tt>true</tt> if the region represents sequencing data.
	 */
	public boolean isSequencing() {
		return holeStatus == 0;
	}

	public BigDecimal getQuality() {
		return BigDecimal.valueOf(regionScore / 1000f).setScale(3, BigDecimal.ROUND_HALF_UP);
	}
}