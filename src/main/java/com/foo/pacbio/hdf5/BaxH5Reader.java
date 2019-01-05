package com.foo.pacbio.hdf5;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import com.foo.pacbio.fastq.FastqRawRecord;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;

/**
 * Convenience class for reading PacBio HDF5 bax file and converting sequencing
 * data to FASTQ format.
 */
public class BaxH5Reader implements Iterable<FastqRawRecord>, AutoCloseable {

	/**
	 * ZMW hole number constant.
	 */
	private static final String ZMW_HOLE_NUMBER_PATH = "PulseData/BaseCalls/ZMW/HoleNumber";

	/**
	 * ZMW num event constant.
	 */
	private static final String ZMW_NUM_EVENT_PATH = "PulseData/BaseCalls/ZMW/NumEvent";

	/**
	 * Region path constant.
	 */
	private static final String REGIONS_PATH = "PulseData/Regions";

	/**
	 * Quality value constant.
	 */
	private static final String QUALITY_VALUE_PATH = "PulseData/BaseCalls/QualityValue";

	/**
	 * Basecall constant.
	 */
	private static final String BASECALL_PATH = "PulseData/BaseCalls/Basecall";

	/**
	 * Type of ZMW that produced the data.
	 */
	private static final String ZMW_HOLE_STATUS_PATH = "PulseData/BaseCalls/ZMW/HoleStatus";

	private final IHDF5SimpleReader reader;

	private final String movieName;

	/**
	 * Creates a new <tt>BaxH5Reader</tt>, given the <tt>Path</tt> to read from.
	 * 
	 * @param file the <tt>Path</tt> to read from
	 */
	public BaxH5Reader(final Path file) {
		String name = file.getFileName().toString();

		this.movieName = name.indexOf('.') >= 0 ? name.substring(0, name.indexOf('.')) : name;
		this.reader = HDF5Factory.openForReading(file.toFile());
	}

	@Override
	public void close() {
		if (reader != null) {
			reader.close();
		}
	}

	@Override
	public Iterator<FastqRawRecord> iterator() {
		return new ReadIterator();
	}

	/**
	 * Iterates the PacBio HDF5 BAX file and converts the sequencing data to FASTQ
	 * records.
	 */
	private class ReadIterator implements Iterator<FastqRawRecord> {

		private final Iterator<Region> regions;

		private final int[] holeNumber;
		private final int[] numEvents;
		private final byte[] seq;
		private final byte[] qual;
		private int shift = 0;
		private int baseShift = 0;

		private final Queue<FastqRawRecord> queue = new LinkedBlockingDeque<>();

		ReadIterator() {
			this.regions = new RegionIterator();
			this.holeNumber = reader.readIntArray(BaxH5Reader.ZMW_HOLE_NUMBER_PATH);
			this.numEvents = reader.readIntArray(BaxH5Reader.ZMW_NUM_EVENT_PATH);
			this.seq = reader.readAsByteArray(BaxH5Reader.BASECALL_PATH);
			this.qual = reader.readAsByteArray(BaxH5Reader.QUALITY_VALUE_PATH); // to be shifted by 33
		}

		@Override
		public boolean hasNext() {
			while (queue.isEmpty() && regions.hasNext()) {

				Region region = regions.next();

				if (region.isSequencing()) {
					for (RegionInterval entry : region.getBoundaries()) {
						int b = entry.getBegin();
						int e = entry.getEnd();
						for (int i = b; i < e; ++i) {
							qual[baseShift + i] += 33;
						}

						StringBuilder builder = new StringBuilder();
						String id = builder
								.append('@')
								.append(movieName)
								.append('/')
								.append(holeNumber[shift])
								.append('/')
								.append(b)
								.append('_')
								.append(e).toString();

						queue.add(new FastqRawRecord(id, seq, qual, baseShift + b, e - b));
					}
				}

				baseShift += numEvents[shift];
				++shift;
			}

			return !queue.isEmpty();
		}

		@Override
		public FastqRawRecord next() {
			if (hasNext()) {
				return queue.remove();
			}
			throw new NoSuchElementException("No read available");
		}
	}

	/**
	 * Iterates over the group that includes information about the Regions table.
	 */
	private class RegionIterator implements Iterator<Region> {

		private int curr = 0;
		private final int[] regionData;
		private final byte[] holeStatus;
		private int minHole = Integer.MAX_VALUE;

		public RegionIterator() {
			this.regionData = reader.readIntArray(BaxH5Reader.REGIONS_PATH);
			this.holeStatus = reader.readAsByteArray(BaxH5Reader.ZMW_HOLE_STATUS_PATH);
		}

		@Override
		public boolean hasNext() {
			return curr < regionData.length;
		}

		@Override
		public Region next() {
			int holeNumber = regionData[curr];
			minHole = Math.min(holeNumber, minHole);
			int next = curr + 5;
			while (next < regionData.length && regionData[next] == holeNumber) {
				next += 5;
			}

			if (next < curr || (next - curr) % 5 != 0) {
				throw new NoSuchElementException("Unexpected region data");
			}

			Region r = new Region(regionData, curr, next, holeStatus[holeNumber - minHole]);
			curr = next;
			return r;
		}
	}
}