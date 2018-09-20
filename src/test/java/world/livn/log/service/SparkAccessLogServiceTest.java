package world.livn.log.service;

import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SparkAccessLogServiceTest {
	static SparkSession spark;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		spark=SparkSession
				.builder()
				.appName("Java Spark SQL basic example")
				.config("spark.master", "local[*]")
				.getOrCreate();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testRunBasicDataFrameExample() {

		try {
			SparkAccessLogService.runBasicDataFrameExample(this.spark);
		} catch (AnalysisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
