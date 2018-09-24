package world.livn.log.service;

import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class SparkAccessLogServiceTest {
	static SparkSession spark;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		spark = SparkSession.builder().appName("Java Spark SQL basic example").config("spark.master", "local[*]")
				.getOrCreate();

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
