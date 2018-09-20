package com.livngds.spark.sql;

import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

public class JavaSparkSQLExampleTest {

	@Test
	public void test() {
		// $example on:init_session$
		SparkSession spark = SparkSession
				.builder()
				.appName("Java Spark SQL basic example")
				.config("spark.master", "local[*]")
				.getOrCreate();

		try {
			JavaSparkSQLExample.runBasicDataFrameExample(spark);
		} catch (AnalysisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		runDatasetCreationExample(spark);
		//		runInferSchemaExample(spark);
		//		runProgrammaticSchemaExample(spark);

		//spark.stop();
	}

}
