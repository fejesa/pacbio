package com.foo.pacbio.hdf5;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class BaxH5ConverterTest {
	
	@Test
	public void test() throws Exception {
		BaxH5Converter converter = new BaxH5Converter();
        Path output = Paths.get("/data/pacbio/ABCDRDQ_Pool_180.fastq");
        List<Path> hds = Files.list(Paths.get("/data/pacbio/input")).collect(Collectors.toList());

        for (Path p : hds) {
            converter.singlePart(p, output, true);
        }
	}
}