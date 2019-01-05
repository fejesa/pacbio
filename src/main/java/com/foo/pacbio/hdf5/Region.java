package com.foo.pacbio.hdf5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sequence region boundaries and scoring value. PacBio can
 * generate different kind of region data therefore we store the status of it
 * for filtering; i.e. for FASTQ conversion.
 */
class Region {

	/** List of region boundaries. */
	private final List<IntPair> beginEnd;

	private final byte holeStatus;
	private final int regionScore;

	public Region(int[] data, int begin, int end, byte holeStatus) {

		this.holeStatus = holeStatus;

		int hqStart = Integer.MAX_VALUE;
		int hqEnd = -1;

		List<IntPair> pairs = new ArrayList<>();

		int score = -1;
		for (int shift = begin; shift < end; shift += 5) {
			int locStart = data[shift + 2];
			int locEnd = data[shift + 3];

			if (data[shift + 1] == 1) { // 'Insert data'
				pairs.add(new IntPair(locStart, locEnd));

			} else if (data[shift + 1] == 2) { // 'HQRegion'
				hqStart = Math.min(locStart, hqStart);
				hqEnd = Math.max(locEnd, hqEnd);
				score = Math.max(score, data[shift + 4]);
			}
		}
		
		this.beginEnd = new ArrayList<>();

		for (final IntPair pair : pairs) {
			int b = Integer.max(pair.first, hqStart);
			int e = Integer.min(pair.second, hqEnd);

			if (e > b) {
				beginEnd.add(new IntPair(b, e));
			}
		}

		regionScore = score;
	}

	public List<IntPair> getBeginEnd() {
		return beginEnd;
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