package world.livn.log.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import scala.Tuple2;

@RunWith(JUnitPlatform.class)
public class SparkAccessLogServiceTest {
	// static SparkSession spark;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		// spark = SparkSession.builder().appName("Java Spark SQL basic
		// example").config("spark.master", "local[*]")
		// .getOrCreate();

	}

	@Test
	public void testRunBasicDataFrameExample() {

		String str = "IP: 192.155.83.114 , Host Name: gds1.livngds.com , Login Name: livn , Internal API User Host=192.155.83.114, Roles=[WEBFRONT, HAL, GDS, CMS] , Geolocation: US United States: Fremont 37.5483,-121.9886";

		// Pattern pattern =
		// Pattern.compile("(/([?!Geolocation])([\\w\\s]+):[\\w\\.\\s]+[^:])|([\\w\\s]+=[\\[]*[\\w\\.\\,\\s]+[\\]*])|([\\w\\s]+=[\\w\\.\\,\\s]+)");
		Pattern pattern = Pattern.compile(
				"(([\\w\\s]+):[\\w\\.\\s]+(:[\\w\\s\\.\\,]+[-\\w\\s\\.]+)*)|([\\w\\s]+=[\\[]+[\\w\\.\\,\\s]+[\\]+])|([\\w\\s]+=[\\w\\.\\s]+)");
		java.util.regex.Matcher m = pattern.matcher(str);
		List<Tuple2<String,String>> list = new ArrayList<>();
		while (m.find()) {
			String li = m.group();
			li = li.replace("=", ":");

			String[] split = li.split(":", 2);

			Tuple2<String,String> jo = new Tuple2<>(split[0],split[1]);
			list.add(jo);

		}
		for (Tuple2<String,String> str1 : list) {
			System.out.println(str1);
		}

	}

}
