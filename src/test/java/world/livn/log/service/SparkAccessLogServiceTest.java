package world.livn.log.service;

import java.util.regex.Pattern;

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
		//		spark = SparkSession.builder().appName("Java Spark SQL basic example").config("spark.master", "local[*]")
		//				.getOrCreate();

	}

	@Test
	public void testRunBasicDataFrameExample() {


		String str="IP: 192.155.83.114 , Host Name: gds1.livngds.com , Login Name: livn , Internal API User Host=192.155.83.114, Roles=[WEBFRONT, HAL, GDS, CMS] , Geolocation: US United States: Fremont 37.5483,-121.9886";

		Pattern pattern = Pattern.compile("^[a-z]*[:|=][A-Za-z0-9\\.\\:\\,]$");
		String [] split =pattern.split(str);
		System.out.println(split.toString());
		for (String string : split) {
			System.out.println(string);
		}
	}

}
